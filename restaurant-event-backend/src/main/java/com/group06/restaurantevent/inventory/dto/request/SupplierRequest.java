package com.group06.restaurantevent.inventory.dto.request;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record SupplierRequest(
    @NotBlank @Size(max=150) String name,
    @NotBlank @Size(max=100) String contactPerson,
    @NotBlank @Pattern(regexp="[0-9]{10}", message="Phone number must contain exactly 10 digits") String phone,
    @NotBlank @Email @Size(max=150) String email,
    @NotBlank @Size(max=500) String address,
    @NotBlank @Size(max=500) String suppliedProducts,
    @NotNull @PastOrPresent(message="Joined date cannot be in the future") LocalDate joinedDate,
    @NotNull Boolean active
) {}
