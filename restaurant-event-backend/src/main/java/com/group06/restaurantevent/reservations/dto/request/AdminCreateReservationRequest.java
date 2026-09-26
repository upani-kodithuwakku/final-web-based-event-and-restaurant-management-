package com.group06.restaurantevent.reservations.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Staff books a table on behalf of an existing customer account. */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminCreateReservationRequest extends CreateReservationRequest {

    @NotBlank @Email
    private String customerEmail;
}
