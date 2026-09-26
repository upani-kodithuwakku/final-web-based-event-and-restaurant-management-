package com.group06.restaurantevent.reservations.dto.response;

import com.group06.restaurantevent.common.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private String bookingReference;
    private Long customerId;
    private String customerName;
    private TableResponse table;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int guestCount;
    private String seatingPreference;
    private String specialRequest;
    private ReservationStatus status;
    private String contactName;
    private String contactPhone;
    private String cancelReason;
    private LocalDateTime createdAt;
}
