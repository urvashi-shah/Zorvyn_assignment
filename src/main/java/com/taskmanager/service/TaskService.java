package com.taskmanager.service;

import com.taskmanager.dto.task.TaskCreateRequest;
import com.taskmanager.dto.task.TaskResponse;
import com.taskmanager.dto.task.TaskUpdateRequest;
import com.taskmanager.model.Task;
import com.taskmanager.repository.TaskRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final OpenAiService openAiService;

    public TaskService(TaskRepository taskRepository, OpenAiService openAiService) {
        this.taskRepository = taskRepository;
        this.openAiService = openAiService;
    }

    public TaskResponse create(TaskCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        String userId = currentUserId();

        OpenAiService.AiEnrichment ai = openAiService.enrichTaskDescription(request.getDescription());

        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setPriority(ai.priority());
        task.setSuggestion(ai.suggestion());
        task.setCreatedAt(Instant.now());

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public List<TaskResponse> getAllForCurrentUser() {
        String userId = currentUserId();
        return taskRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse update(String taskId, TaskUpdateRequest request) {
        String userId = currentUserId();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!userId.equals(task.getUserId())) {
            throw new IllegalArgumentException("You do not have access to this task");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            task.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    public void delete(String taskId) {
        String userId = currentUserId();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!userId.equals(task.getUserId())) {
            throw new IllegalArgumentException("You do not have access to this task");
        }

        taskRepository.deleteById(taskId);
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        // In our JWT setup, username == userId
        return authentication.getName();
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getSuggestion(),
                task.getCreatedAt()
        );
    }
}

