package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateRequest;
import hexlet.code.dto.task.TaskResponse;
import hexlet.code.dto.task.TaskUpdateRequest;
import java.util.List;

public interface TaskService {

    List<TaskResponse> getTasks(String titleCont, Long assigneeId, String status, Long labelId);

    TaskResponse getTask(Long id);

    TaskResponse createTask(TaskCreateRequest request);

    TaskResponse updateTask(Long id, TaskUpdateRequest request);

    void deleteTask(Long id);
}
