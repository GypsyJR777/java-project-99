package hexlet.code.service;

import hexlet.code.dto.taskstatus.TaskStatusCreateRequest;
import hexlet.code.dto.taskstatus.TaskStatusResponse;
import hexlet.code.dto.taskstatus.TaskStatusUpdateRequest;
import java.util.List;

public interface TaskStatusService {

    List<TaskStatusResponse> getAllTaskStatuses();

    TaskStatusResponse getTaskStatus(Long id);

    TaskStatusResponse createTaskStatus(TaskStatusCreateRequest request);

    TaskStatusResponse updateTaskStatus(Long id, TaskStatusUpdateRequest request);

    void deleteTaskStatus(Long id);

    void createDefaultsIfMissing();
}
