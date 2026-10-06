package com.group06.restaurantevent.payment.strategy;

import com.group06.restaurantevent.common.enums.PaymentOption;
import com.group06.restaurantevent.payment.dto.request.CardDetailsRequest;
import com.group06.restaurantevent.payment.entity.CustomerPayment;

/**
 * Strategy pattern: each payment method decides how a payment is settled.
 * Adding a method means adding a strategy, not changing CustomerPaymentService.
 */
public interface PaymentMethodStrategy {
    PaymentOption option();

    /** Applies this method to the payment, setting status and method-specific fields. */
    void apply(CustomerPayment payment, CardDetailsRequest card);
}
