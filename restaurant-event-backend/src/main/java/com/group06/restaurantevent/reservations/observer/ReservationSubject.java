package com.group06.restaurantevent.reservations.observer;

/** Subject: the three operations shown in the Observer lecture. */
public interface ReservationSubject {
    void addObserver(ReservationObserver observer);
    void removeObserver(ReservationObserver observer);
    void notifyObservers(ReservationEvent event);
}
