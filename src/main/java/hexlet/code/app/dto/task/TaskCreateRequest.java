package hexlet.code.app.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TaskCreateRequest(
    Integer index,
    @JsonProperty("assignee_id") Long assigneeId,
    @NotBlank
    @Size(min = 1)
    String title,

    String content,
    @NotBlank
    @Size(min = 1)
    String status,

    List<Long> taskLabelIds
) {
}
