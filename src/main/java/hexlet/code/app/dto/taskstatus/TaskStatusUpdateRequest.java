package hexlet.code.app.dto.taskstatus;

import jakarta.validation.constraints.Size;

public class TaskStatusUpdateRequest {

    @Size(min = 1)
    private String name;

    @Size(min = 1)
    private String slug;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
