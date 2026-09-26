package com.group06.restaurantevent.reservations.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Staff edit: same optional fields as the customer edit, plus the option to move to another table. */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUpdateReservationRequest extends UpdateReservationRequest {

    private Long tableId;
}
