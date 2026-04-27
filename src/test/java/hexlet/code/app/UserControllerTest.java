package hexlet.code.app;

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

import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

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
	void welcomeIsAvailableWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/welcome"))
			.andExpect(status().isOk())
			.andExpect(content().string("Welcome to Spring"));
	}

	@Test
	void rejectsUnauthorizedUsersRequests() throws Exception {
		mockMvc.perform(get("/api/users"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void createsUser() throws Exception {
		var adminToken = loginAs("hexlet@example.com", "qwerty");
		var body = """
			{
			  "email": "jack@google.com",
			  "firstName": "Jack",
			  "lastName": "Jons",
			  "password": "some-password"
			}
			""";

		mockMvc.perform(post("/api/users")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.email").value("jack@google.com"))
			.andExpect(jsonPath("$.firstName").value("Jack"))
			.andExpect(jsonPath("$.lastName").value("Jons"))
			.andExpect(jsonPath("$.password").doesNotExist());

		var createdUser = userRepository.findByEmail("jack@google.com").orElseThrow();
		org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("some-password", createdUser.getPassword())).isTrue();
	}

	@Test
	void getsUserById() throws Exception {
		var adminToken = loginAs("hexlet@example.com", "qwerty");
		var user = new User();
		user.setEmail("john@google.com");
		user.setFirstName("John");
		user.setLastName("Doe");
		user.setPassword(passwordEncoder.encode("secret"));
		var savedUser = userRepository.save(user);

		mockMvc.perform(get("/api/users/{id}", savedUser.getId())
				.header("Authorization", bearer(adminToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(savedUser.getId()))
			.andExpect(jsonPath("$.email").value("john@google.com"))
			.andExpect(jsonPath("$.firstName").value("John"))
			.andExpect(jsonPath("$.lastName").value("Doe"))
			.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void getsUsersList() throws Exception {
		var adminToken = loginAs("hexlet@example.com", "qwerty");
		var firstUser = new User();
		firstUser.setEmail("john@google.com");
		firstUser.setFirstName("John");
		firstUser.setLastName("Doe");
		firstUser.setPassword(passwordEncoder.encode("secret"));
		userRepository.save(firstUser);

		var secondUser = new User();
		secondUser.setEmail("jack@yahoo.com");
		secondUser.setFirstName("Jack");
		secondUser.setLastName("Jons");
		secondUser.setPassword(passwordEncoder.encode("secret"));
		userRepository.save(secondUser);

		mockMvc.perform(get("/api/users").header("Authorization", bearer(adminToken)))
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
		var adminToken = loginAs("hexlet@example.com", "qwerty");
		var user = new User();
		user.setEmail("jack@google.com");
		user.setFirstName("Jack");
		user.setLastName("Jons");
		user.setPassword(passwordEncoder.encode("some-password"));
		var savedUser = userRepository.save(user);

		var body = """
			{
			  "email": "jack@yahoo.com",
			  "password": "new-password"
			}
			""";

		mockMvc.perform(put("/api/users/{id}", savedUser.getId())
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
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
		org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("new-password", updatedUser.getPassword())).isTrue();
	}

	@Test
	void deletesUser() throws Exception {
		var adminToken = loginAs("hexlet@example.com", "qwerty");
		var user = new User();
		user.setEmail("delete@google.com");
		user.setPassword(passwordEncoder.encode("secret"));
		var savedUser = userRepository.save(user);

		mockMvc.perform(delete("/api/users/{id}", savedUser.getId())
				.header("Authorization", bearer(adminToken)))
			.andExpect(status().isNoContent());

		org.assertj.core.api.Assertions.assertThat(userRepository.findById(savedUser.getId())).isEmpty();
	}

	@Test
	void returnsBadRequestForInvalidCreatePayload() throws Exception {
		var adminToken = loginAs("hexlet@example.com", "qwerty");
		var body = """
			{
			  "email": "not-an-email",
			  "password": "qw"
			}
			""";

		mockMvc.perform(post("/api/users")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.email").exists())
			.andExpect(jsonPath("$.password").exists());
	}

	@Test
	void returnsBadRequestForInvalidUpdatePayload() throws Exception {
		var adminToken = loginAs("hexlet@example.com", "qwerty");
		var user = new User();
		user.setEmail("update@google.com");
		user.setPassword(passwordEncoder.encode("secret"));
		var savedUser = userRepository.save(user);

		var body = """
			{
			  "password": "qw"
			}
			""";

		mockMvc.perform(put("/api/users/{id}", savedUser.getId())
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.password").exists());
	}

	@Test
	void returnsNotFoundForUnknownUser() throws Exception {
		var adminToken = loginAs("hexlet@example.com", "qwerty");

		mockMvc.perform(get("/api/users/{id}", 999999)
				.header("Authorization", bearer(adminToken)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("User not found"));
	}

	@Test
	void forbidsUpdatingAnotherUser() throws Exception {
		var adminToken = loginAs("hexlet@example.com", "qwerty");

		mockMvc.perform(post("/api/users")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "user1@example.com",
					  "password": "secret123"
					}
					"""))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/api/users")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "user2@example.com",
					  "password": "secret123"
					}
					"""))
			.andExpect(status().isCreated());

		var token = loginAs("user1@example.com", "secret123");
		var anotherUser = userRepository.findByEmail("user2@example.com").orElseThrow();

		mockMvc.perform(put("/api/users/{id}", anotherUser.getId())
				.header("Authorization", bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "firstName": "Hacker"
					}
					"""))
			.andExpect(status().isForbidden());
	}

	@Test
	void forbidsDeletingAnotherUser() throws Exception {
		var adminToken = loginAs("hexlet@example.com", "qwerty");

		mockMvc.perform(post("/api/users")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "user1@example.com",
					  "password": "secret123"
					}
					"""))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/api/users")
				.header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "user2@example.com",
					  "password": "secret123"
					}
					"""))
			.andExpect(status().isCreated());

		var token = loginAs("user1@example.com", "secret123");
		var anotherUser = userRepository.findByEmail("user2@example.com").orElseThrow();

		mockMvc.perform(delete("/api/users/{id}", anotherUser.getId())
				.header("Authorization", bearer(token)))
			.andExpect(status().isForbidden());
	}

	private String loginAs(String username, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "username": "%s",
					  "password": "%s"
					}
					""".formatted(username, password)))
			.andExpect(status().isOk())
			.andReturn();

		return result.getResponse().getContentAsString();
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}
}
