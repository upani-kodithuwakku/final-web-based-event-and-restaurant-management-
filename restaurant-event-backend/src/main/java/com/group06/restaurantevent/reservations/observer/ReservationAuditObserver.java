package com.group06.restaurantevent.reservations.observer;

import com.group06.restaurantevent.common.audit.AuditLog;
import com.group06.restaurantevent.common.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** ConcreteObserver: preserve the change and the user who performed it. */
@Component
@RequiredArgsConstructor
public class ReservationAuditObserver implements ReservationObserver {
    private final AuditLogRepository auditLogs;

    public void update(ReservationEvent event) {
        auditLogs.save(AuditLog.builder()
                .userId(event.actorId()).action("RESERVATION_" + event.action())
                .entityName("TableReservation").entityId(event.reservationId())
                .oldValue(event.oldStatus() == null ? null : event.oldStatus().name())
                .newValue(event.newStatus().name()).build());
    }
}
