package com.group06.restaurantevent.reservations.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTableRequest {
    @NotBlank
    private String tableNumber;

    @NotNull @Min(1)
    private Integer capacity;

    private String location;
}
