package hexlet.code.app.dto.label;

import java.time.LocalDate;

public class LabelResponse {

    private Long id;

    private String name;

    private LocalDate createdAt;

    public LabelResponse(Long id, String name, LocalDate createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
}
