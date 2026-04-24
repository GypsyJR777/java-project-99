package hexlet.code.app.config;

import hexlet.code.app.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

	@Bean
	public CommandLineRunner createAdmin(UserService userService) {
		return args -> userService.createAdminIfMissing();
	}
}
