package hexlet.code.dto.label;

import java.time.LocalDate;

public record LabelResponse(
    Long id,
    String name,
    LocalDate createdAt
) {
}
