package hexlet.code.app.controller;

import hexlet.code.app.dto.task.TaskCreateRequest;
import hexlet.code.app.dto.task.TaskResponse;
import hexlet.code.app.dto.task.TaskUpdateRequest;
import hexlet.code.app.service.TaskService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> index(
        @RequestParam(required = false) String titleCont,
        @RequestParam(required = false) Long assigneeId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long labelId
    ) {
        var tasks = taskService.getTasks(titleCont, assigneeId, status, labelId);
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(tasks.size()))
            .body(tasks);
    }

    @GetMapping("/{id}")
    public TaskResponse show(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskCreateRequest request) {
        return taskService.createTask(request);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request) {
        return taskService.updateTask(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
