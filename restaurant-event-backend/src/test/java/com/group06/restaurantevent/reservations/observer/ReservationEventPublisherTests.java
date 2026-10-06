package com.group06.restaurantevent.reservations.observer;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class ReservationEventPublisherTests {
    @Test void notifiesBothObserversAndSupportsUnsubscribeWithoutDuplicateSubscriptions() {
        List<String> reactions = new ArrayList<>();
        ReservationObserver notification = event -> reactions.add("notification");
        ReservationObserver audit = event -> reactions.add("audit");
        var subject = new ReservationEventPublisher(List.of(notification, audit));
        subject.addObserver(notification);
        subject.notifyObservers(new ReservationEvent(1L,"RES-1",null,2L,"CREATED",null,null));
        assertThat(reactions).containsExactly("notification","audit");
        subject.removeObserver(notification);
        reactions.clear();
        subject.notifyObservers(new ReservationEvent(1L,"RES-1",null,2L,"UPDATED",null,null));
        assertThat(reactions).containsExactly("audit");
    }

    @Test void propagatesFailuresSoTheReservationTransactionCanRollBack() {
        var subject = new ReservationEventPublisher(List.of(event -> {throw new IllegalStateException("audit failed");}));
        assertThatThrownBy(() -> subject.notifyObservers(null))
                .isInstanceOf(IllegalStateException.class).hasMessage("audit failed");
    }
}
