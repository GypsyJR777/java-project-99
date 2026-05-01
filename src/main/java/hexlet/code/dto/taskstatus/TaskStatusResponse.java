package hexlet.code.dto.taskstatus;

import java.time.LocalDate;

public record TaskStatusResponse(
    Long id,
    String name,
    String slug,
    LocalDate createdAt
) {
}
