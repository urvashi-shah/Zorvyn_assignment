package com.taskmanager.dto.task;

import com.taskmanager.model.AiSuggestion;
import com.taskmanager.model.TaskPriority;
import java.time.Instant;

public class TaskResponse {
    private String id;
    private String title;
    private String description;
    private TaskPriority priority;
    private AiSuggestion suggestion;
    private Instant createdAt;

    public TaskResponse() {
    }

    public TaskResponse(
            String id,
            String title,
            String description,
            TaskPriority priority,
            AiSuggestion suggestion,
            Instant createdAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.suggestion = suggestion;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public AiSuggestion getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(AiSuggestion suggestion) {
        this.suggestion = suggestion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

