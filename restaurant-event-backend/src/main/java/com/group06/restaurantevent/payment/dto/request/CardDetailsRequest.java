package com.group06.restaurantevent.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Simulated card details. Only the last four digits are ever stored. */
@Data
public class CardDetailsRequest {
    @NotBlank(message = "Name on card is required")
    @Size(max = 100, message = "Name on card must be 100 characters or fewer")
    private String holderName;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "(?:[0-9] *){12}", message = "Card number must have exactly 12 digits")
    private String number;

    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "(0[1-9]|1[0-2])/[0-9]{2}", message = "Expiry date must be in MM/YY format")
    private String expiry;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "[0-9]{3,4}", message = "CVV must be 3 or 4 digits")
    private String cvv;
}
