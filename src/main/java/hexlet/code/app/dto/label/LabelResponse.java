package hexlet.code.app.dto.label;

import java.time.LocalDate;

public record LabelResponse(
    Long id,
    String name,
    LocalDate createdAt
) {
}
