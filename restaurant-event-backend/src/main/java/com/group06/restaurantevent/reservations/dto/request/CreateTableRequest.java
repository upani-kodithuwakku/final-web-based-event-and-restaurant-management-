package com.group06.restaurantevent.reservations.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateTableRequest {
    @NotBlank
    @Size(max=20)
    private String tableNumber;

    @NotNull @Min(1)
    @Max(200)
    private Integer capacity;

    @Size(max=50)
    private String location;

    @Size(max=100)
    private String displayName;
    @Size(max=255)
    private String description;
    @Size(max=255)
    @Pattern(regexp="^(https://[^\\s]+|/images/[^\\s]+)?$", message="Use an HTTPS image URL or a local /images/ path")
    private String imageUrl;
}
