package hexlet.code.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hexlet.code.app.model.Task;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LabelControllerTest extends ControllerTestSupport {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Test
    void createsDefaultLabelsOnStartup() {
        assertThat(labelRepository.findByName("feature")).isPresent();
        assertThat(labelRepository.findByName("bug")).isPresent();
    }

    @Test
    void rejectsUnauthorizedLabelRequests() throws Exception {
        mockMvc.perform(get("/api/labels"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "new label"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getsLabelsList() throws Exception {
        var token = adminToken();

        performAuthorized(get("/api/labels"), token)
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "2"))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].name", containsInAnyOrder("feature", "bug")));
    }

    @Test
    void getsLabelById() throws Exception {
        var token = adminToken();
        var label = labelRepository.findByName("feature").orElseThrow();

        performAuthorized(get("/api/labels/{id}", label.getId()), token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(label.getId()))
            .andExpect(jsonPath("$.name").value("feature"))
            .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createsLabel() throws Exception {
        var token = adminToken();

        performJson(post("/api/labels"), token, """
                    {
                      "name": "new label"
                    }
                    """)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("new label"))
            .andExpect(jsonPath("$.createdAt").exists());

        assertThat(labelRepository.findByName("new label")).isPresent();
    }

    @Test
    void updatesLabelPartially() throws Exception {
        var token = adminToken();
        var label = labelRepository.findByName("feature").orElseThrow();

        performJson(put("/api/labels/{id}", label.getId()), token, """
                    {
                      "name": "feature updated"
                    }
                    """)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(label.getId()))
            .andExpect(jsonPath("$.name").value("feature updated"));

        assertThat(labelRepository.findByName("feature updated")).isPresent();
    }

    @Test
    void keepsLabelUnchangedForEmptyUpdate() throws Exception {
        var token = adminToken();
        var label = labelRepository.findByName("feature").orElseThrow();

        performJson(put("/api/labels/{id}", label.getId()), token, "{}")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(label.getId()))
            .andExpect(jsonPath("$.name").value("feature"));

        assertThat(labelRepository.findByName("feature")).isPresent();
    }

    @Test
    void deletesLabel() throws Exception {
        var token = adminToken();
        var label = labelRepository.findByName("bug").orElseThrow();

        performAuthorized(delete("/api/labels/{id}", label.getId()), token)
            .andExpect(status().isNoContent());

        assertThat(labelRepository.findById(label.getId())).isEmpty();
    }

    @Test
    void preventsDeletingLinkedLabel() throws Exception {
        var token = adminToken();
        var label = labelRepository.findByName("feature").orElseThrow();
        saveTaskWithLabel(label.getId());

        performAuthorized(delete("/api/labels/{id}", label.getId()), token)
            .andExpect(status().isBadRequest());

        assertThat(labelRepository.findById(label.getId())).isPresent();
    }

    @Test
    void returnsBadRequestForInvalidCreatePayload() throws Exception {
        var token = adminToken();

        performJson(post("/api/labels"), token, """
                    {
                      "name": "ab"
                    }
                    """)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void returnsBadRequestForDuplicateName() throws Exception {
        var token = adminToken();

        performJson(post("/api/labels"), token, """
                    {
                      "name": "feature"
                    }
                    """)
            .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownLabel() throws Exception {
        var token = adminToken();

        performAuthorized(get("/api/labels/{id}", 999999), token)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Label not found"));
    }

    private void saveTaskWithLabel(Long labelId) {
        var task = new Task();
        task.setName("Task 1");
        task.setTaskStatus(taskStatusRepository.findBySlug("draft").orElseThrow());
        task.getLabels().add(labelRepository.findById(labelId).orElseThrow());
        taskRepository.save(task);
    }
}
