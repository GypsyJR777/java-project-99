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
class TaskStatusControllerTest extends ControllerTestSupport {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Test
    void createsDefaultTaskStatusesOnStartup() {
        assertThat(taskStatusRepository.findBySlug("draft")).isPresent();
        assertThat(taskStatusRepository.findBySlug("to_review")).isPresent();
        assertThat(taskStatusRepository.findBySlug("to_be_fixed")).isPresent();
        assertThat(taskStatusRepository.findBySlug("to_publish")).isPresent();
        assertThat(taskStatusRepository.findBySlug("published")).isPresent();
    }

    @Test
    void rejectsUnauthorizedTaskStatusRequests() throws Exception {
        mockMvc.perform(get("/api/task_statuses"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "New",
                      "slug": "new"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getsTaskStatusesList() throws Exception {
        var token = adminToken();

        performAuthorized(get("/api/task_statuses"), token)
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "5"))
            .andExpect(jsonPath("$", hasSize(5)))
            .andExpect(jsonPath("$[*].slug", containsInAnyOrder(
                "draft",
                "to_review",
                "to_be_fixed",
                "to_publish",
                "published"
            )));
    }

    @Test
    void getsTaskStatusById() throws Exception {
        var token = adminToken();
        var taskStatus = taskStatusRepository.findBySlug("to_review").orElseThrow();

        performAuthorized(get("/api/task_statuses/{id}", taskStatus.getId()), token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(taskStatus.getId()))
            .andExpect(jsonPath("$.name").value("ToReview"))
            .andExpect(jsonPath("$.slug").value("to_review"))
            .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void createsTaskStatus() throws Exception {
        var token = adminToken();

        performJson(post("/api/task_statuses"), token, """
                    {
                      "name": "New",
                      "slug": "new"
                    }
                    """)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("New"))
            .andExpect(jsonPath("$.slug").value("new"))
            .andExpect(jsonPath("$.createdAt").exists());

        assertThat(taskStatusRepository.findBySlug("new")).isPresent();
    }

    @Test
    void updatesTaskStatusPartially() throws Exception {
        var token = adminToken();
        var taskStatus = taskStatusRepository.findBySlug("draft").orElseThrow();

        performJson(put("/api/task_statuses/{id}", taskStatus.getId()), token, """
                    {
                      "name": "NewStatus"
                    }
                    """)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(taskStatus.getId()))
            .andExpect(jsonPath("$.name").value("NewStatus"))
            .andExpect(jsonPath("$.slug").value("draft"));

        var updatedTaskStatus = taskStatusRepository.findById(taskStatus.getId()).orElseThrow();
        assertThat(updatedTaskStatus.getName()).isEqualTo("NewStatus");
        assertThat(updatedTaskStatus.getSlug()).isEqualTo("draft");
    }

    @Test
    void updatesTaskStatusSlugOnly() throws Exception {
        var token = adminToken();
        var taskStatus = taskStatusRepository.findBySlug("to_publish").orElseThrow();

        performJson(put("/api/task_statuses/{id}", taskStatus.getId()), token, """
                    {
                      "slug": "ready_to_publish"
                    }
                    """)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("ToPublish"))
            .andExpect(jsonPath("$.slug").value("ready_to_publish"));

        assertThat(taskStatusRepository.findBySlug("ready_to_publish")).isPresent();
    }

    @Test
    void deletesTaskStatus() throws Exception {
        var token = adminToken();
        var taskStatus = taskStatusRepository.findBySlug("published").orElseThrow();

        performAuthorized(delete("/api/task_statuses/{id}", taskStatus.getId()), token)
            .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.findById(taskStatus.getId())).isEmpty();
    }

    @Test
    void returnsBadRequestForInvalidCreatePayload() throws Exception {
        var token = adminToken();

        performJson(post("/api/task_statuses"), token, """
                    {
                      "name": "",
                      "slug": ""
                    }
                    """)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.slug").exists());
    }

    @Test
    void returnsBadRequestForDuplicateNameOrSlug() throws Exception {
        var token = adminToken();

        performJson(post("/api/task_statuses"), token, """
                    {
                      "name": "Draft",
                      "slug": "another_draft"
                    }
                    """)
            .andExpect(status().isBadRequest());

        performJson(post("/api/task_statuses"), token, """
                    {
                      "name": "Another Draft",
                      "slug": "draft"
                    }
                    """)
            .andExpect(status().isBadRequest());
    }

    @Test
    void returnsBadRequestForInvalidUpdatePayload() throws Exception {
        var token = adminToken();
        var taskStatus = taskStatusRepository.findBySlug("draft").orElseThrow();

        performJson(put("/api/task_statuses/{id}", taskStatus.getId()), token, """
                    {
                      "slug": ""
                    }
                    """)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.slug").exists());
    }

    @Test
    void returnsNotFoundForUnknownTaskStatus() throws Exception {
        var token = adminToken();

        performAuthorized(get("/api/task_statuses/{id}", 999999), token)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Task status not found"));
    }
}
