package hexlet.code.app.config;

import hexlet.code.app.service.LabelService;
import hexlet.code.app.service.TaskStatusService;
import hexlet.code.app.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeData(
        UserService userService,
        TaskStatusService taskStatusService,
        LabelService labelService
    ) {
        return args -> {
            userService.createAdminIfMissing();
            taskStatusService.createDefaultsIfMissing();
            labelService.createDefaultsIfMissing();
        };
    }
}
