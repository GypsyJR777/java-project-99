package hexlet.code.service;

import hexlet.code.dto.user.UserCreateRequest;
import hexlet.code.dto.user.UserResponse;
import hexlet.code.dto.user.UserUpdateRequest;
import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUser(Long id);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    boolean canManageUser(Long id, String username);

    void createAdminIfMissing();
}
