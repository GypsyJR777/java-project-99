package hexlet.code.config;

import hexlet.code.service.LabelService;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
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
