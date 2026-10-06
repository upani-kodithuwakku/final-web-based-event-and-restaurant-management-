package com.group06.restaurantevent.payment.strategy;

import com.group06.restaurantevent.common.enums.PaymentOption;
import com.group06.restaurantevent.common.enums.PaymentStatus;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.payment.dto.request.CardDetailsRequest;
import com.group06.restaurantevent.payment.entity.CustomerPayment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Simulated card gateway for the project: any 12-digit card number with a valid
 * expiry and CVV is approved and settled immediately.
 */
@Component
public class CardPaymentStrategy implements PaymentMethodStrategy {

    @Override
    public PaymentOption option() { return PaymentOption.CARD; }

    @Override
    public void apply(CustomerPayment payment, CardDetailsRequest card) {
        if (card == null)
            throw new BadRequestException("Card details are required to pay by card");

        if (card.getHolderName() == null || card.getHolderName().isBlank() || card.getHolderName().length() > 100)
            throw new BadRequestException("Enter a cardholder name of up to 100 characters");
        if (card.getNumber() == null)
            throw new BadRequestException("Card number is required");
        if (card.getExpiry() == null || !card.getExpiry().matches("(0[1-9]|1[0-2])/[0-9]{2}"))
            throw new BadRequestException("Expiry date must be in MM/YY format");
        if (card.getCvv() == null || !card.getCvv().matches("[0-9]{3,4}"))
            throw new BadRequestException("CVC must have 3 or 4 digits");
        String digits = card.getNumber().replace(" ", "");
        if (!digits.matches("\\d{12}"))
            throw new BadRequestException("Card number must have exactly 12 digits");
        if (expiry(card.getExpiry()).isBefore(YearMonth.now(java.time.ZoneId.of("Asia/Colombo"))))
            throw new BadRequestException("Card has expired");

        // The full number and CVV are used for validation only and never stored.
        payment.setMethod(PaymentOption.CARD);
        payment.setStatus(PaymentStatus.PAID);
        payment.setCardHolderName(card.getHolderName().trim());
        payment.setCardLast4(digits.substring(digits.length() - 4));
        payment.setCardBrand(brand(digits));
        payment.setGatewayReference("SIM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        payment.setPaidAt(LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo")));
    }

    private YearMonth expiry(String mmYy) {
        String[] parts = mmYy.split("/");
        return YearMonth.of(2000 + Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
    }

    private String brand(String digits) {
        if (digits.startsWith("4")) return "VISA";
        if (digits.matches("^(5[1-5]|222[1-9]|22[3-9]\\d|2[3-6]\\d{2}|27[01]\\d|2720).*")) return "MASTERCARD";
        if (digits.startsWith("34") || digits.startsWith("37")) return "AMEX";
        return "CARD";
    }
}
