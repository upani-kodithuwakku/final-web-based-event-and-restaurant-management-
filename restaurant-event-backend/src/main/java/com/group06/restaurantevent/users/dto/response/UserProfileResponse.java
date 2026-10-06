package com.group06.restaurantevent.users.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private List<String> roles;
    private LocalDateTime createdAt;
}
