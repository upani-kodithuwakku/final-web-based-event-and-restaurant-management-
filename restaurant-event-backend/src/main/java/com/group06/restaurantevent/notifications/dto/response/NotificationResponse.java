package com.group06.restaurantevent.notifications.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime createdAt;

    // Lombok would expose this as "read"; the frontend expects "isRead".
    @JsonProperty("isRead")
    public boolean isRead() { return isRead; }
}
