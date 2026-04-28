package hexlet.code.app.repository;

import hexlet.code.app.model.TaskStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {

    Optional<TaskStatus> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
