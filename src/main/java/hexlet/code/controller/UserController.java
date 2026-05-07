package hexlet.code.controller;

import hexlet.code.dto.user.UserCreateRequest;
import hexlet.code.dto.user.UserResponse;
import hexlet.code.dto.user.UserUpdateRequest;
import hexlet.code.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> index() {
        var users = userService.getAllUsers();
        return ControllerResponses.withTotalCount(users);
    }

    @GetMapping("/{id}")
    public UserResponse show(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@userService.canManageUser(#id, authentication.name)")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@userService.canManageUser(#id, authentication.name)")
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
