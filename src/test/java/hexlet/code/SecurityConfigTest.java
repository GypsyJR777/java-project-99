package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import hexlet.code.config.SecurityConfig;
import hexlet.code.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;

class SecurityConfigTest {

    @Test
    void disablesJwtFilterServletRegistration() {
        var securityConfig = new SecurityConfig();
        var jwtAuthenticationFilter = mock(JwtAuthenticationFilter.class);

        var registration = securityConfig.jwtFilterRegistration(jwtAuthenticationFilter);

        assertThat(registration.isEnabled()).isFalse();
        assertThat(registration.getFilter()).isSameAs(jwtAuthenticationFilter);
    }
}
