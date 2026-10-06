package com.group06.restaurantevent.notifications.mapper;

import com.group06.restaurantevent.notifications.dto.response.NotificationResponse;
import com.group06.restaurantevent.notifications.entity.Notification;
import org.springframework.stereotype.Component;

/** Converts Notification entities to the API response DTO in one place. */
@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUser().getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
