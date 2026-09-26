package com.group06.restaurantevent.reservations.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateReservationRequest {
    @FutureOrPresent
    private LocalDate reservationDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Min(1)
    private Integer guestCount;

    private String seatingPreference;
    private String specialRequest;
    private String contactName;
    private String contactPhone;
}
