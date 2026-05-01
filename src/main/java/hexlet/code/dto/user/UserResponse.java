package hexlet.code.dto.user;

import java.time.LocalDate;

public record UserResponse(
    Long id,
    String email,
    String firstName,
    String lastName,
    LocalDate createdAt
) {
}
