package hexlet.code.app.service;

import hexlet.code.app.dto.task.TaskCreateRequest;
import hexlet.code.app.dto.task.TaskResponse;
import hexlet.code.app.dto.task.TaskUpdateRequest;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;

    public TaskService(
        TaskRepository taskRepository,
        TaskStatusRepository taskStatusRepository,
        UserRepository userRepository,
        LabelRepository labelRepository
    ) {
        this.taskRepository = taskRepository;
        this.taskStatusRepository = taskStatusRepository;
        this.userRepository = userRepository;
        this.labelRepository = labelRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(String titleCont, Long assigneeId, String status, Long labelId) {
        return taskRepository.findAll(buildFilter(titleCont, assigneeId, status, labelId))
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
        task.setName(request.title());
        task.setIndex(request.index());
        task.setDescription(request.content());
        task.setTaskStatus(findTaskStatus(request.status()));
        task.setAssignee(findAssignee(request.assigneeId()));
        task.setLabels(findLabels(request.taskLabelIds()));
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        var task = findTask(id);
        if (request.title() != null) {
            task.setName(request.title());
        }
        if (request.index() != null) {
            task.setIndex(request.index());
        }
        if (request.content() != null) {
            task.setDescription(request.content());
        }
        if (request.status() != null) {
            task.setTaskStatus(findTaskStatus(request.status()));
        }
        if (request.assigneeId() != null) {
            task.setAssignee(findAssignee(request.assigneeId()));
        }
        if (request.taskLabelIds() != null) {
            task.setLabels(findLabels(request.taskLabelIds()));
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

    private Set<Label> findLabels(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        var labels = labelRepository.findAllById(ids);
        var foundIds = labels.stream()
            .map(Label::getId)
            .collect(Collectors.toSet());
        var requestedIds = new LinkedHashSet<>(ids);
        if (!foundIds.containsAll(requestedIds)) {
            throw new ResourceNotFoundException("Label not found");
        }
        return new LinkedHashSet<>(labels);
    }

    private Specification<Task> buildFilter(String titleCont, Long assigneeId, String status, Long labelId) {
        return Specification.allOf(
            titleContains(titleCont),
            hasAssignee(assigneeId),
            hasStatus(status),
            hasLabel(labelId)
        );
    }

    private Specification<Task> titleContains(String titleCont) {
        return (root, query, criteriaBuilder) -> {
            if (titleCont == null || titleCont.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            var pattern = "%" + titleCont.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
        };
    }

    private Specification<Task> hasAssignee(Long assigneeId) {
        return (root, query, criteriaBuilder) -> {
            if (assigneeId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId);
        };
    }

    private Specification<Task> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("taskStatus").get("slug"), status);
        };
    }

    private Specification<Task> hasLabel(Long labelId) {
        return (root, query, criteriaBuilder) -> {
            if (labelId == null) {
                return criteriaBuilder.conjunction();
            }
            query.distinct(true);
            return criteriaBuilder.equal(root.join("labels").get("id"), labelId);
        };
    }

    private TaskResponse toResponse(Task task) {
        var assignee = task.getAssignee();
        var taskStatus = task.getTaskStatus();
        var taskLabelIds = task.getLabels()
            .stream()
            .map(Label::getId)
            .sorted()
            .toList();
        return new TaskResponse(
            task.getId(),
            task.getIndex(),
            task.getCreatedAt() == null ? null : task.getCreatedAt().toLocalDate(),
            assignee == null ? null : assignee.getId(),
            task.getName(),
            task.getDescription(),
            taskStatus.getSlug(),
            taskLabelIds
        );
    }
}
