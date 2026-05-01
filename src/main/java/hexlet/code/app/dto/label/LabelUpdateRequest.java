package hexlet.code.app.dto.label;

import jakarta.validation.constraints.Size;

public record LabelUpdateRequest(
    @Size(min = 3, max = 1000)
    String name
) {
}
