package com.zorvyn.finance.controller;

import com.zorvyn.finance.dto.CreateUserRequest;
import com.zorvyn.finance.dto.UpdateUserRequest;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.create(request);
        return toResponse(user);
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return userService.list().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @PatchMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return toResponse(userService.update(id, request));
    }

    private Map<String, Object> toResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "active", user.isActive()
        );
    }
}
