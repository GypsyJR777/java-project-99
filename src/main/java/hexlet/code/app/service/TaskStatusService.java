package hexlet.code.app.service;

import hexlet.code.app.dto.taskstatus.TaskStatusCreateRequest;
import hexlet.code.app.dto.taskstatus.TaskStatusResponse;
import hexlet.code.app.dto.taskstatus.TaskStatusUpdateRequest;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;

    public TaskStatusService(TaskStatusRepository taskStatusRepository) {
        this.taskStatusRepository = taskStatusRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskStatusResponse> getAllTaskStatuses() {
        return taskStatusRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TaskStatusResponse getTaskStatus(Long id) {
        return toResponse(findTaskStatus(id));
    }

    @Transactional
    public TaskStatusResponse createTaskStatus(TaskStatusCreateRequest request) {
        var taskStatus = new TaskStatus();
        taskStatus.setName(request.getName());
        taskStatus.setSlug(request.getSlug());
        return toResponse(taskStatusRepository.save(taskStatus));
    }

    @Transactional
    public TaskStatusResponse updateTaskStatus(Long id, TaskStatusUpdateRequest request) {
        var taskStatus = findTaskStatus(id);
        if (request.getName() != null) {
            taskStatus.setName(request.getName());
        }
        if (request.getSlug() != null) {
            taskStatus.setSlug(request.getSlug());
        }
        return toResponse(taskStatusRepository.save(taskStatus));
    }

    @Transactional
    public void deleteTaskStatus(Long id) {
        var taskStatus = findTaskStatus(id);
        taskStatusRepository.delete(taskStatus);
    }

    @Transactional
    public void createDefaultsIfMissing() {
        createDefaultIfMissing("Draft", "draft");
        createDefaultIfMissing("ToReview", "to_review");
        createDefaultIfMissing("ToBeFixed", "to_be_fixed");
        createDefaultIfMissing("ToPublish", "to_publish");
        createDefaultIfMissing("Published", "published");
    }

    private void createDefaultIfMissing(String name, String slug) {
        if (taskStatusRepository.existsBySlug(slug)) {
            return;
        }
        var taskStatus = new TaskStatus();
        taskStatus.setName(name);
        taskStatus.setSlug(slug);
        taskStatusRepository.save(taskStatus);
    }

    private TaskStatus findTaskStatus(Long id) {
        return taskStatusRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task status not found"));
    }

    private TaskStatusResponse toResponse(TaskStatus taskStatus) {
        return new TaskStatusResponse(
            taskStatus.getId(),
            taskStatus.getName(),
            taskStatus.getSlug(),
            taskStatus.getCreatedAt() == null ? null : taskStatus.getCreatedAt().toLocalDate()
        );
    }
}
