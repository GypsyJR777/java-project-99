package hexlet.code.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    String firstName,
    String lastName,

    @Email
    String email,

    @Size(min = 3)
    String password
) {
}
