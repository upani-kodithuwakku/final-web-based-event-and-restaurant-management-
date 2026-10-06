package com.group06.restaurantevent.reports.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class ReservationReportResponse {
    private LocalDate from;
    private LocalDate to;
    private long totalReservations;
    private long totalGuests;
    /** Count per status, in ReservationStatus order (zero counts included). */
    private Map<String, Long> byStatus;
    /** NO_SHOW as a percentage of reservations that reached their time (completed + no-show). */
    private double noShowRate;
    private LocalDate busiestDay;
    private long busiestDayReservations;
}
