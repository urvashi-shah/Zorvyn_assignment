package com.zorvyn.finance.dto;

import com.zorvyn.finance.model.Role;
import jakarta.validation.constraints.NotNull;

public class UpdateUserRequest {
    @NotNull
    private Role role;

    @NotNull
    private Boolean active;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
