package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hexlet.code.repository.UserRepository;
import hexlet.code.security.AppUserDetails;
import hexlet.code.security.AppUserDetailsService;
import hexlet.code.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class SecurityServiceTest {

    private static final String JWT_SECRET = "12345678901234567890123456789012";

    @Test
    void rejectsTokenForAnotherUser() {
        var jwtService = new JwtService(JWT_SECRET);
        var owner = new AppUserDetails(1L, "owner@example.com", "secret");
        var anotherUser = new AppUserDetails(2L, "another@example.com", "secret");
        var token = jwtService.generateToken(owner);

        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }

    @Test
    void throwsForUnknownUser() {
        var userRepository = mock(UserRepository.class);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        var userDetailsService = new AppUserDetailsService(userRepository);

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("User not found");
    }
}
