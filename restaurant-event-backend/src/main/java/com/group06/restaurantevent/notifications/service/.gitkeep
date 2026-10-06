package com.group06.restaurantevent.notifications.service;

import com.group06.restaurantevent.notifications.entity.Notification;
import com.group06.restaurantevent.notifications.repository.NotificationRepository;
import com.group06.restaurantevent.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern: creates and persists typed notifications from a single place.
 */
@Component
@RequiredArgsConstructor
public class NotificationFactory {

    private final NotificationRepository notificationRepository;

    public Notification reservationConfirmed(User user, String reference) {
        return save(user,
                "Reservation Confirmed",
                "Your reservation " + reference + " has been confirmed.",
                "RESERVATION");
    }

    public Notification reservationCancelled(User user, String reference) {
        return save(user,
                "Reservation Cancelled",
                "Your reservation " + reference + " has been cancelled.",
                "RESERVATION");
    }

    public Notification reservationCheckedIn(User user, String reference) {
        return save(user,
                "Checked In",
                "You have been checked in for reservation " + reference + ".",
                "RESERVATION");
    }

    public Notification lowStock(User user, String itemName) {
        return save(user,
                "Low Stock Alert",
                "Inventory item '" + itemName + "' has fallen below reorder level.",
                "INVENTORY");
    }

    public Notification paymentReceived(User user, String invoiceNumber, String amount) {
        return save(user,
                "Payment Received",
                "Payment of " + amount + " for invoice " + invoiceNumber + " was successful.",
                "PAYMENT");
    }

    private Notification save(User user, String title, String message, String type) {
        Notification n = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        return notificationRepository.save(n);
    }
}
