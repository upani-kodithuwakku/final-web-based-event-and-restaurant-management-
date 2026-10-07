package com.group06.restaurantevent.notifications.service;

import com.group06.restaurantevent.common.exception.ForbiddenException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.notifications.dto.response.NotificationResponse;
import com.group06.restaurantevent.notifications.entity.Notification;
import com.group06.restaurantevent.notifications.mapper.NotificationMapper;
import com.group06.restaurantevent.notifications.repository.NotificationRepository;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForUser(String email) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(findActiveUser(email).getId())
                .stream().map(notificationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String email) {
        return notificationRepository.countByUserIdAndIsReadFalse(findActiveUser(email).getId());
    }

    @Transactional
    public NotificationResponse markRead(Long id, String email) {
        Long userId = findActiveUser(email).getId();
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!n.getUser().getId().equals(userId))
            throw new ForbiddenException("Access denied");
        n.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(n));
    }

    @Transactional
    public void markAllRead(String email) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(findActiveUser(email).getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteAll(String email) { notificationRepository.deleteByUserId(findActiveUser(email).getId()); }

    @Transactional
    public void deleteOne(Long id, String email) {
        Long userId = findActiveUser(email).getId();
        Notification note = notificationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!note.getUser().getId().equals(userId)) throw new ForbiddenException("Access denied");
        notificationRepository.delete(note);
    }

    private User findActiveUser(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
