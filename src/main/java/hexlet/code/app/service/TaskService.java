package hexlet.code.app.service;

import hexlet.code.app.dto.task.TaskCreateRequest;
import hexlet.code.app.dto.task.TaskResponse;
import hexlet.code.app.dto.task.TaskUpdateRequest;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;

    public TaskService(
        TaskRepository taskRepository,
        TaskStatusRepository taskStatusRepository,
        UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        return toResponse(findTask(id));
    }

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        var task = new Task();
        task.setName(request.getTitle());
        task.setIndex(request.getIndex());
        task.setDescription(request.getContent());
        task.setTaskStatus(findTaskStatus(request.getStatus()));
        task.setAssignee(findAssignee(request.getAssigneeId()));
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        var task = findTask(id);
        if (request.getTitle() != null) {
            task.setName(request.getTitle());
        }
        if (request.getIndex() != null) {
            task.setIndex(request.getIndex());
        }
        if (request.getContent() != null) {
            task.setDescription(request.getContent());
        }
        if (request.getStatus() != null) {
            task.setTaskStatus(findTaskStatus(request.getStatus()));
        }
        if (request.getAssigneeId() != null) {
            task.setAssignee(findAssignee(request.getAssigneeId()));
        }
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        var task = findTask(id);
        taskRepository.delete(task);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private hexlet.code.app.model.TaskStatus findTaskStatus(String slug) {
        return taskStatusRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Task status not found"));
    }

    private User findAssignee(Long id) {
        if (id == null) {
            return null;
        }
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private TaskResponse toResponse(Task task) {
        var assignee = task.getAssignee();
        var taskStatus = task.getTaskStatus();
        return new TaskResponse(
            task.getId(),
            task.getIndex(),
            task.getCreatedAt() == null ? null : task.getCreatedAt().toLocalDate(),
            assignee == null ? null : assignee.getId(),
            task.getName(),
            task.getDescription(),
            taskStatus.getSlug()
        );
    }
}
