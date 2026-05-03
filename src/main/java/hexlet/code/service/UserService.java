package hexlet.code.service;

import hexlet.code.dto.user.UserCreateRequest;
import hexlet.code.dto.user.UserResponse;
import hexlet.code.dto.user.UserUpdateRequest;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return toResponse(findUser(id));
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        var user = new User();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPassword(passwordEncoder.encode(request.password()));
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        var user = findUser(id);
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        var user = findUser(id);
        userRepository.delete(user);
    }

    @Transactional
    public void createAdminIfMissing() {
        var adminEmail = "hexlet@example.com";
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        var user = new User();
        user.setEmail(adminEmail);
        user.setPassword(passwordEncoder.encode("qwerty"));
        userRepository.save(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getCreatedAt() == null ? null : user.getCreatedAt().toLocalDate()
        );
    }
}
