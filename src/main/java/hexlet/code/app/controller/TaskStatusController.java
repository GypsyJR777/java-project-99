package hexlet.code.app.controller;

import hexlet.code.app.dto.taskstatus.TaskStatusCreateRequest;
import hexlet.code.app.dto.taskstatus.TaskStatusResponse;
import hexlet.code.app.dto.taskstatus.TaskStatusUpdateRequest;
import hexlet.code.app.service.TaskStatusService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/task_statuses")
public class TaskStatusController {

    private final TaskStatusService taskStatusService;

    public TaskStatusController(TaskStatusService taskStatusService) {
        this.taskStatusService = taskStatusService;
    }

    @GetMapping
    public ResponseEntity<List<TaskStatusResponse>> index() {
        var taskStatuses = taskStatusService.getAllTaskStatuses();
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(taskStatuses.size()))
            .body(taskStatuses);
    }

    @GetMapping("/{id}")
    public TaskStatusResponse show(@PathVariable Long id) {
        return taskStatusService.getTaskStatus(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusResponse create(@Valid @RequestBody TaskStatusCreateRequest request) {
        return taskStatusService.createTaskStatus(request);
    }

    @PutMapping("/{id}")
    public TaskStatusResponse update(@PathVariable Long id, @Valid @RequestBody TaskStatusUpdateRequest request) {
        return taskStatusService.updateTaskStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskStatusService.deleteTaskStatus(id);
    }
}
