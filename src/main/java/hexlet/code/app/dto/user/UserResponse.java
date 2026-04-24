package hexlet.code.app.dto.user;

import java.time.LocalDate;

public class UserResponse {

	private Long id;

	private String email;

	private String firstName;

	private String lastName;

	private LocalDate createdAt;

	public UserResponse(Long id, String email, String firstName, String lastName, LocalDate createdAt) {
		this.id = id;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}
}
