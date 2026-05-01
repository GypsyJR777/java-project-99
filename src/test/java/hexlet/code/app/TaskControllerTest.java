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
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskControllerTest extends ControllerTestSupport {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Test
    void rejectsUnauthorizedTaskRequests() throws Exception {
        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Task",
                      "status": "draft"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createsTask() throws Exception {
        var token = adminToken();
        var admin = userRepository.findByEmail("hexlet@example.com").orElseThrow();
        var feature = labelRepository.findByName("feature").orElseThrow();
        var bug = labelRepository.findByName("bug").orElseThrow();

        performJson(post("/api/tasks"), token, """
                    {
                      "index": 12,
                      "assignee_id": %d,
                      "title": "Test title",
                      "content": "Test content",
                      "status": "draft",
                      "taskLabelIds": [%d, %d]
                    }
                    """.formatted(admin.getId(), feature.getId(), bug.getId()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.index").value(12))
            .andExpect(jsonPath("$.assignee_id").value(admin.getId()))
            .andExpect(jsonPath("$.title").value("Test title"))
            .andExpect(jsonPath("$.content").value("Test content"))
            .andExpect(jsonPath("$.status").value("draft"))
            .andExpect(jsonPath("$.taskLabelIds", hasSize(2)))
            .andExpect(jsonPath("$.taskLabelIds[*]", containsInAnyOrder(
                feature.getId().intValue(),
                bug.getId().intValue()
            )))
            .andExpect(jsonPath("$.createdAt").exists());

        var task = taskRepository.findAll().getFirst();
        assertThat(task.getName()).isEqualTo("Test title");
        assertThat(task.getDescription()).isEqualTo("Test content");
        assertThat(task.getTaskStatus().getSlug()).isEqualTo("draft");
        assertThat(task.getAssignee().getId()).isEqualTo(admin.getId());
        assertThat(taskRepository.existsByLabelsId(feature.getId())).isTrue();
        assertThat(taskRepository.existsByLabelsId(bug.getId())).isTrue();
    }

    @Test
    void getsTaskById() throws Exception {
        var token = adminToken();
        var admin = userRepository.findByEmail("hexlet@example.com").orElseThrow();
        var savedTask = saveTask("Task 1", "Description of task 1", 3140, "to_be_fixed", admin.getId());

        performAuthorized(get("/api/tasks/{id}", savedTask.getId()), token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedTask.getId()))
            .andExpect(jsonPath("$.index").value(3140))
            .andExpect(jsonPath("$.assignee_id").value(admin.getId()))
            .andExpect(jsonPath("$.title").value("Task 1"))
            .andExpect(jsonPath("$.content").value("Description of task 1"))
            .andExpect(jsonPath("$.status").value("to_be_fixed"))
            .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getsTasksList() throws Exception {
        var token = adminToken();
        var admin = userRepository.findByEmail("hexlet@example.com").orElseThrow();
        saveTask("Task 1", "Description of task 1", 3140, "to_be_fixed", admin.getId());
        saveTask("Task 2", "Description of task 2", 3161, "to_review", admin.getId());

        performAuthorized(get("/api/tasks"), token)
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "2"))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title").value("Task 1"))
            .andExpect(jsonPath("$[0].status").value("to_be_fixed"))
            .andExpect(jsonPath("$[1].title").value("Task 2"))
            .andExpect(jsonPath("$[1].status").value("to_review"));
    }

    @Test
    void filtersTasksByQueryParams() throws Exception {
        var token = adminToken();
        var admin = userRepository.findByEmail("hexlet@example.com").orElseThrow();
        var otherUser = saveUser("other@example.com");
        var bug = labelRepository.findByName("bug").orElseThrow();
        var feature = labelRepository.findByName("feature").orElseThrow();

        var target = saveTaskWithLabel("Create new version", "Content", 1, "to_be_fixed", admin.getId(), bug.getId());
        saveTaskWithLabel("Create docs", "Content", 2, "to_be_fixed", otherUser.getId(), bug.getId());
        saveTaskWithLabel("Create draft", "Content", 3, "draft", admin.getId(), bug.getId());
        saveTaskWithLabel("Create feature", "Content", 4, "to_be_fixed", admin.getId(), feature.getId());
        saveTaskWithLabel("Publish version", "Content", 5, "to_be_fixed", admin.getId(), bug.getId());

        performAuthorized(get("/api/tasks")
                .param("titleCont", "create")
                .param("assigneeId", String.valueOf(admin.getId()))
                .param("status", "to_be_fixed")
                .param("labelId", String.valueOf(bug.getId())), token)
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "1"))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(target.getId()))
            .andExpect(jsonPath("$[0].title").value("Create new version"))
            .andExpect(jsonPath("$[0].assignee_id").value(admin.getId()))
            .andExpect(jsonPath("$[0].status").value("to_be_fixed"))
            .andExpect(jsonPath("$[0].taskLabelIds[0]").value(bug.getId()));
    }

    @Test
    void updatesTaskPartially() throws Exception {
        var token = adminToken();
        var admin = userRepository.findByEmail("hexlet@example.com").orElseThrow();
        var feature = labelRepository.findByName("feature").orElseThrow();
        var savedTask = saveTask("Task 1", "Old content", 12, "draft", admin.getId());

        performJson(put("/api/tasks/{id}", savedTask.getId()), token, """
                    {
                      "title": "New title",
                      "content": "New content",
                      "status": "to_review",
                      "taskLabelIds": [%d]
                    }
                    """.formatted(feature.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedTask.getId()))
            .andExpect(jsonPath("$.index").value(12))
            .andExpect(jsonPath("$.assignee_id").value(admin.getId()))
            .andExpect(jsonPath("$.title").value("New title"))
            .andExpect(jsonPath("$.content").value("New content"))
            .andExpect(jsonPath("$.status").value("to_review"))
            .andExpect(jsonPath("$.taskLabelIds[0]").value(feature.getId()));

        var updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        assertThat(updatedTask.getName()).isEqualTo("New title");
        assertThat(updatedTask.getDescription()).isEqualTo("New content");
        assertThat(updatedTask.getTaskStatus().getSlug()).isEqualTo("to_review");
        assertThat(taskRepository.existsByLabelsId(feature.getId())).isTrue();
    }

    @Test
    void deletesTask() throws Exception {
        var token = adminToken();
        var savedTask = saveTask("Task 1", "Content", 1, "draft", null);

        performAuthorized(delete("/api/tasks/{id}", savedTask.getId()), token)
            .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(savedTask.getId())).isEmpty();
    }

    @Test
    void returnsBadRequestForInvalidCreatePayload() throws Exception {
        var token = adminToken();

        performJson(post("/api/tasks"), token, """
                    {
                      "title": "",
                      "status": ""
                    }
                    """)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").exists())
            .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void returnsNotFoundForUnknownReferences() throws Exception {
        var token = adminToken();

        performJson(post("/api/tasks"), token, """
                    {
                      "title": "Task",
                      "status": "unknown"
                    }
                    """)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Task status not found"));

        performJson(post("/api/tasks"), token, """
                    {
                      "title": "Task",
                      "status": "draft",
                      "assignee_id": 999999
                    }
                    """)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("User not found"));

        performJson(post("/api/tasks"), token, """
                    {
                      "title": "Task",
                      "status": "draft",
                      "taskLabelIds": [999999]
                    }
                    """)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Label not found"));
    }

    @Test
    void preventsDeletingLinkedUser() throws Exception {
        var token = adminToken();
        var admin = userRepository.findByEmail("hexlet@example.com").orElseThrow();
        saveTask("Task 1", "Content", 1, "draft", admin.getId());

        performAuthorized(delete("/api/users/{id}", admin.getId()), token)
            .andExpect(status().isBadRequest());

        assertThat(userRepository.findById(admin.getId())).isPresent();
    }

    @Test
    void preventsDeletingLinkedTaskStatus() throws Exception {
        var token = adminToken();
        var taskStatus = taskStatusRepository.findBySlug("draft").orElseThrow();
        saveTask("Task 1", "Content", 1, "draft", null);

        performAuthorized(delete("/api/task_statuses/{id}", taskStatus.getId()), token)
            .andExpect(status().isBadRequest());

        assertThat(taskStatusRepository.findById(taskStatus.getId())).isPresent();
    }

    @Test
    void returnsNotFoundForUnknownTask() throws Exception {
        var token = adminToken();

        performAuthorized(get("/api/tasks/{id}", 999999), token)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Task not found"));
    }

    private Task saveTask(String title, String content, Integer index, String status, Long assigneeId) {
        var task = new Task();
        task.setName(title);
        task.setDescription(content);
        task.setIndex(index);
        task.setTaskStatus(taskStatusRepository.findBySlug(status).orElseThrow());
        if (assigneeId != null) {
            task.setAssignee(userRepository.findById(assigneeId).orElseThrow());
        }
        return taskRepository.save(task);
    }

    private Task saveTaskWithLabel(
        String title,
        String content,
        Integer index,
        String status,
        Long assigneeId,
        Long labelId
    ) {
        var task = saveTask(title, content, index, status, assigneeId);
        task.getLabels().add(labelRepository.findById(labelId).orElseThrow());
        return taskRepository.save(task);
    }

    private User saveUser(String email) {
        var user = new User();
        user.setEmail(email);
        user.setPassword("password");
        return userRepository.save(user);
    }
}
