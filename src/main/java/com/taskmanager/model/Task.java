package com.taskmanager.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Beginner note:
 * This represents a MongoDB document in the "tasks" collection.
 *
 * Fields requested:
 * id, title, description, priority, suggestion, createdAt
 *
 * We also store userId so tasks belong to a specific logged-in user.
 */
@Document(collection = "tasks")
public class Task {

    @Id
    private String id;

    private String userId;

    private String title;
    private String description;

    private TaskPriority priority;

    private AiSuggestion suggestion;

    private Instant createdAt;

    public Task() {
    }

    public Task(
            String id,
            String userId,
            String title,
            String description,
            TaskPriority priority,
            AiSuggestion suggestion,
            Instant createdAt
    ) {
        this.id = id;
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

