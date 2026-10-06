package com.group06.restaurantevent.reservations.observer;

import com.group06.restaurantevent.common.enums.ReservationStatus;
import com.group06.restaurantevent.users.entity.User;

/** The change shared with every observer. It is local to one request. */
public record ReservationEvent(Long reservationId, String reference, User customer,
                               Long actorId, String action,
                               ReservationStatus oldStatus, ReservationStatus newStatus) {}
