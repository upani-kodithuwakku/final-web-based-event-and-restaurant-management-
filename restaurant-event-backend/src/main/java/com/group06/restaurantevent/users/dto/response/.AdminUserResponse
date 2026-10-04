package com.group06.restaurantevent.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** Same JSON shape the admin Users page already uses. */
@Data
@Builder
public class AdminUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private List<String> roles;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Lombok would expose this as "active"; the frontend expects "isActive".
    @JsonProperty("isActive")
    public boolean isActive() { return isActive; }
}
