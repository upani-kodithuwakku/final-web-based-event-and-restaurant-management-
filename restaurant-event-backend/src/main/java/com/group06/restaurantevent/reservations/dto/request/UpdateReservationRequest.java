package com.group06.restaurantevent.reservations.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
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
    @Max(200)
    private Integer guestCount;

    @Size(max = 50)
    private String seatingPreference;
    @Size(max=500)
    private String specialRequest;
    @Pattern(regexp = ".*\\S.*", message="Contact name cannot be blank") @Size(max=100)
    private String contactName;
    @Pattern(regexp = "[0-9]{10}", message = "Phone number must contain exactly 10 digits")
    private String contactPhone;
}
