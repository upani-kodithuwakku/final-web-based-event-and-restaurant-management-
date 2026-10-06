package com.group06.restaurantevent.reservations.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateReservationRequest {

    @NotNull
    @Positive
    private Long tableId;

    @NotNull @FutureOrPresent(message = "Reservation date cannot be in the past")
    private LocalDate reservationDate;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull @Min(1)
    @Max(200)
    private Integer guestCount;

    @Size(max=50)
    private String seatingPreference;
    @Size(max=500)
    private String specialRequest;

    @NotBlank
    @Size(max=100)
    private String contactName;

    @NotBlank
    @Pattern(regexp = "[0-9]{10}", message = "Phone number must contain exactly 10 digits")
    private String contactPhone;
}
