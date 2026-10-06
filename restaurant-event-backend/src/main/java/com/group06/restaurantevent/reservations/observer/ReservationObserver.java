package com.group06.restaurantevent.reservations.observer;

/** Observer: each subscriber decides how to react to a reservation change. */
public interface ReservationObserver {
    void update(ReservationEvent event);
}
