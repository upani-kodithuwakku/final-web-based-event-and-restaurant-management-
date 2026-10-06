package com.group06.restaurantevent.reservations.observer;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** ConcreteSubject. Spring supplies the observers when the application starts. */
@Component
public class ReservationEventPublisher implements ReservationSubject {
    // Safe to iterate even if an observer is added/removed during another request.
    private final CopyOnWriteArrayList<ReservationObserver> observers = new CopyOnWriteArrayList<>();

    public ReservationEventPublisher(List<ReservationObserver> observers) {
        observers.forEach(this::addObserver);
    }

    public void addObserver(ReservationObserver observer) {
        observers.addIfAbsent(java.util.Objects.requireNonNull(observer));
    }

    public void removeObserver(ReservationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(ReservationEvent event) {
        for (ReservationObserver observer : observers) {
            observer.update(event);
        }
    }
}
