package hexlet.code.app.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class TaskResponse {

    private Long id;

    private Integer index;

    private LocalDate createdAt;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    private String title;

    private String content;

    private String status;

    public TaskResponse(
        Long id,
        Integer index,
        LocalDate createdAt,
        Long assigneeId,
        String title,
        String content,
        String status
    ) {
        this.id = id;
        this.index = index;
        this.createdAt = createdAt;
        this.assigneeId = assigneeId;
        this.title = title;
        this.content = content;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Integer getIndex() {
        return index;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }
}
