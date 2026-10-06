package com.group06.restaurantevent.events.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record EventPackageRequest(@NotBlank @Size(max=100) String name, @NotBlank @Pattern(regexp="[A-Z_]+") @Size(max=100) String eventType, @Size(max=1000) String description, @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal basePrice, @Min(1) @Max(1000) int minimumGuests, @Min(1) @Max(1000) int maximumGuests) {}
