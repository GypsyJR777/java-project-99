package hexlet.code.app.dto.taskstatus;

import java.time.LocalDate;

public class TaskStatusResponse {

    private Long id;

    private String name;

    private String slug;

    private LocalDate createdAt;

    public TaskStatusResponse(Long id, String name, String slug, LocalDate createdAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
}
