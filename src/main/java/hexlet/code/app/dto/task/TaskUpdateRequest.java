package hexlet.code.app.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TaskUpdateRequest(
    Integer index,
    @JsonProperty("assignee_id") Long assigneeId,
    @Size(min = 1)
    String title,

    String content,
    @Size(min = 1)
    String status,

    List<Long> taskLabelIds
) {
}
