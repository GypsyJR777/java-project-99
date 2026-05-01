package hexlet.code.app.dto.label;

import jakarta.validation.constraints.Size;

public class LabelUpdateRequest {

    @Size(min = 3, max = 1000)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
