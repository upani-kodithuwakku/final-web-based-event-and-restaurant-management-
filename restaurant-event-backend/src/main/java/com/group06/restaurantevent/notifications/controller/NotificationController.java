package com.group06.restaurantevent.notifications.controller;

import com.group06.restaurantevent.notifications.dto.response.NotificationResponse;
import com.group06.restaurantevent.notifications.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List my notifications, newest first")
    public ResponseEntity<List<NotificationResponse>> list(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(notificationService.listForUser(principal.getUsername()));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Count my unread notifications")
    public ResponseEntity<Map<String, Long>> unreadCount(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(principal.getUsername())));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark one of my notifications as read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(notificationService.markRead(id, principal.getUsername()));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all my notifications as read")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal UserDetails principal) {
        notificationService.markAllRead(principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    /** Remove only the signed-in user's notifications. */
    @DeleteMapping
    public ResponseEntity<Void> clearAll(@AuthenticationPrincipal UserDetails principal) {
        notificationService.deleteAll(principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOne(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        notificationService.deleteOne(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
