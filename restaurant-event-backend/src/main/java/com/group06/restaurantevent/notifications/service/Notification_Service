package com.group06.restaurantevent.notifications.service;

import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.notifications.dto.response.NotificationResponse;
import com.group06.restaurantevent.notifications.entity.Notification;
import com.group06.restaurantevent.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> listForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public NotificationResponse markRead(Long id, Long userId) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        n.setRead(true);
        return toResponse(notificationRepository.save(n));
    }

    @Transactional
    public void clearAll(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).userId(n.getUser().getId())
                .title(n.getTitle()).message(n.getMessage())
                .type(n.getType()).isRead(n.isRead())
                .createdAt(n.getCreatedAt()).build();
    }
}
