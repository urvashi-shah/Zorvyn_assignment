package com.taskmanager.controller;

import com.taskmanager.dto.task.TaskCreateRequest;
import com.taskmanager.dto.task.TaskResponse;
import com.taskmanager.dto.task.TaskUpdateRequest;
import com.taskmanager.service.TaskService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@RequestBody TaskCreateRequest request) {
        return ResponseEntity.ok(taskService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAll() {
        return ResponseEntity.ok(taskService.getAllForCurrentUser());
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(@PathVariable String taskId, @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.update(taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> delete(@PathVariable String taskId) {
        taskService.delete(taskId);
        return ResponseEntity.ok().body("Task deleted successfully");
    }
}

