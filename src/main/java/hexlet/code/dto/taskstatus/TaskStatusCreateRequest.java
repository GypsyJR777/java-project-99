package hexlet.code.dto.taskstatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskStatusCreateRequest(
    @NotBlank
    @Size(min = 1)
    String name,

    @NotBlank
    @Size(min = 1)
    String slug
) {
}
