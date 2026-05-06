package hexlet.code;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerTest extends ControllerTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createsAdminOnStartup() {
        var admin = userRepository.findByEmail("hexlet@example.com").orElseThrow();

        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("qwerty", admin.getPassword())).isTrue();
    }

    @Test
    void loginReturnsToken() throws Exception {
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "hexlet@example.com",
                      "password": "qwerty"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.isEmptyOrNullString())));
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "hexlet@example.com",
                      "password": "wrong-password"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsUnauthorizedUsersRequests() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMalformedAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Token invalid"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer invalid"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createsUser() throws Exception {
        var adminToken = adminToken();
        performJson(post("/api/users"), adminToken, """
            {
              "email": "jack@google.com",
              "firstName": "Jack",
              "lastName": "Jons",
              "password": "some-password"
            }
            """)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.email").value("jack@google.com"))
            .andExpect(jsonPath("$.firstName").value("Jack"))
            .andExpect(jsonPath("$.lastName").value("Jons"))
            .andExpect(jsonPath("$.password").doesNotExist());

        var createdUser = userRepository.findByEmail("jack@google.com").orElseThrow();
        var passwordMatches = passwordEncoder.matches("some-password", createdUser.getPassword());
        org.assertj.core.api.Assertions.assertThat(passwordMatches).isTrue();
    }

    @Test
    void getsUserById() throws Exception {
        var adminToken = adminToken();
        var savedUser = saveUser("john@google.com", "John", "Doe", "secret");

        performAuthorized(get("/api/users/{id}", savedUser.getId()), adminToken)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()))
            .andExpect(jsonPath("$.email").value("john@google.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getsUsersList() throws Exception {
        var adminToken = adminToken();
        saveUser("john@google.com", "John", "Doe", "secret");
        saveUser("jack@yahoo.com", "Jack", "Jons", "secret");

        performAuthorized(get("/api/users"), adminToken)
            .andExpect(status().isOk())
            .andExpect(header().string("X-Total-Count", "3"))
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[*].email", hasItem("hexlet@example.com")))
            .andExpect(jsonPath("$[*].email", hasItem("john@google.com")))
            .andExpect(jsonPath("$[*].email", hasItem("jack@yahoo.com")))
            .andExpect(jsonPath("$[*].password").doesNotExist());
    }

    @Test
    void updatesUserPartially() throws Exception {
        var savedUser = saveUser("jack@google.com", "Jack", "Jons", "some-password");
        var token = loginAs("jack@google.com", "some-password");

        performJson(put("/api/users/{id}", savedUser.getId()), token, """
            {
              "email": "jack@yahoo.com",
              "password": "new-password"
            }
            """)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()))
            .andExpect(jsonPath("$.email").value("jack@yahoo.com"))
            .andExpect(jsonPath("$.firstName").value("Jack"))
            .andExpect(jsonPath("$.lastName").value("Jons"))
            .andExpect(jsonPath("$.password").doesNotExist());

        var updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updatedUser.getEmail()).isEqualTo("jack@yahoo.com");
        org.assertj.core.api.Assertions.assertThat(updatedUser.getFirstName()).isEqualTo("Jack");
        org.assertj.core.api.Assertions.assertThat(updatedUser.getLastName()).isEqualTo("Jons");
        var passwordMatches = passwordEncoder.matches("new-password", updatedUser.getPassword());
        org.assertj.core.api.Assertions.assertThat(passwordMatches).isTrue();
    }

    @Test
    void updatesUserNamesOnly() throws Exception {
        var savedUser = saveUser("name-only@google.com", "Old", "Name", "secret");
        var token = loginAs("name-only@google.com", "secret");

        performJson(put("/api/users/{id}", savedUser.getId()), token, """
            {
              "firstName": "New",
              "lastName": "Person"
            }
            """)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("name-only@google.com"))
            .andExpect(jsonPath("$.firstName").value("New"))
            .andExpect(jsonPath("$.lastName").value("Person"));

        var updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updatedUser.getFirstName()).isEqualTo("New");
        org.assertj.core.api.Assertions.assertThat(updatedUser.getLastName()).isEqualTo("Person");
    }

    @Test
    void userUpdatesOwnProfile() throws Exception {
        var savedUser = saveUser("self@example.com", "Old", "Name", "secret123");
        var token = loginAs("self@example.com", "secret123");

        performJson(put("/api/users/{id}", savedUser.getId()), token, """
            {
              "firstName": "Self"
            }
            """)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Self"));
    }

    @Test
    void deletesUser() throws Exception {
        var savedUser = saveUser("delete@google.com", null, null, "secret");
        var token = loginAs("delete@google.com", "secret");

        performAuthorized(delete("/api/users/{id}", savedUser.getId()), token)
            .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    void returnsBadRequestForInvalidCreatePayload() throws Exception {
        var adminToken = adminToken();
        performJson(post("/api/users"), adminToken, """
            {
              "email": "not-an-email",
              "password": "qw"
            }
            """)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.email").exists())
            .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void returnsBadRequestForInvalidUpdatePayload() throws Exception {
        var adminToken = adminToken();
        var savedUser = saveUser("update@google.com", null, null, "secret");

        performJson(put("/api/users/{id}", savedUser.getId()), adminToken, """
            {
              "password": "qw"
            }
            """)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void returnsNotFoundForUnknownUser() throws Exception {
        var adminToken = adminToken();

        performAuthorized(get("/api/users/{id}", 999999), adminToken)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void rejectsUpdatingAnotherUser() throws Exception {
        saveUser("user1@example.com", null, null, "secret123");
        var anotherUser = saveUser("user2@example.com", null, null, "secret123");
        var token = loginAs("user1@example.com", "secret123");

        performJson(put("/api/users/{id}", anotherUser.getId()), token, """
                    {
                      "firstName": "Updated"
                    }
                    """)
            .andExpect(status().isForbidden());

        var unchangedUser = userRepository.findById(anotherUser.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(unchangedUser.getFirstName()).isNull();
    }

    @Test
    void rejectsDeletingAnotherUser() throws Exception {
        saveUser("user1@example.com", null, null, "secret123");
        var anotherUser = saveUser("user2@example.com", null, null, "secret123");
        var token = loginAs("user1@example.com", "secret123");

        performAuthorized(delete("/api/users/{id}", anotherUser.getId()), token)
            .andExpect(status().isForbidden());

        org.assertj.core.api.Assertions.assertThat(userRepository.findById(anotherUser.getId())).isPresent();
    }

    private User saveUser(String email, String firstName, String lastName, String password) {
        var user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }
}
