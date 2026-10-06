package com.group06.restaurantevent.reservations.observer;

import com.group06.restaurantevent.notifications.service.NotificationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** ConcreteObserver: create one in-app notification for the customer. */
@Component
@RequiredArgsConstructor
public class CustomerNotificationObserver implements ReservationObserver {
    private final NotificationFactory notifications;

    public void update(ReservationEvent event) {
        notifications.reservationChanged(event.customer(), event.reference(),
                event.action(), event.newStatus().name());
    }
}
