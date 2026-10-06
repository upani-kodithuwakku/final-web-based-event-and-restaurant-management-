package com.group06.restaurantevent.payment.strategy;

import com.group06.restaurantevent.common.enums.PaymentOption;
import com.group06.restaurantevent.common.enums.PaymentStatus;
import com.group06.restaurantevent.payment.dto.request.CardDetailsRequest;
import com.group06.restaurantevent.payment.entity.CustomerPayment;
import org.springframework.stereotype.Component;

/** The customer pays at the restaurant; staff mark it paid when they collect it. */
@Component
public class PayAtOutletPaymentStrategy implements PaymentMethodStrategy {

    @Override
    public PaymentOption option() { return PaymentOption.PAY_AT_OUTLET; }

    @Override
    public void apply(CustomerPayment payment, CardDetailsRequest card) {
        payment.setMethod(PaymentOption.PAY_AT_OUTLET);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCardHolderName(null);
        payment.setCardLast4(null);
        payment.setCardBrand(null);
        payment.setGatewayReference(null);
        payment.setPaidAt(null);
    }
}
