package com.group06.restaurantevent.payment.strategy;

import com.group06.restaurantevent.payment.dto.request.CardDetailsRequest;
import com.group06.restaurantevent.payment.entity.CustomerPayment;
import com.group06.restaurantevent.common.enums.PaymentStatus;
import com.group06.restaurantevent.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import static org.assertj.core.api.Assertions.*;

class PaymentStrategyTests {
    private CardDetailsRequest card() {
        var c = new CardDetailsRequest();
        c.setHolderName(" Customer "); c.setNumber("4242 4242 4242"); c.setCvv("123");
        c.setExpiry(YearMonth.now(ZoneId.of("Asia/Colombo")).plusYears(1).format(DateTimeFormatter.ofPattern("MM/yy")));
        return c;
    }

    @Test void strategiesCanBeSwappedThroughTheSameInterface() {
        var payment = new CustomerPayment();
        PaymentMethodStrategy strategy = new CardPaymentStrategy();
        strategy.apply(payment,card());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getCardLast4()).isEqualTo("4242");
        strategy = new PayAtOutletPaymentStrategy();
        strategy.apply(payment,null);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getCardLast4()).isNull();
        assertThat(payment.getPaidAt()).isNull();
    }

    @Test void strategyRejectsInvalidDetailsEvenWhenCalledWithoutControllerValidation() {
        var strategy = new CardPaymentStrategy();
        assertThatThrownBy(() -> strategy.apply(new CustomerPayment(),null)).isInstanceOf(BadRequestException.class);
        var c = card(); c.setExpiry("13/30");
        assertThatThrownBy(() -> strategy.apply(new CustomerPayment(),c)).isInstanceOf(BadRequestException.class);
        c.setExpiry(null);
        assertThatThrownBy(() -> strategy.apply(new CustomerPayment(),c)).isInstanceOf(BadRequestException.class);
        c.setExpiry(card().getExpiry()); c.setCvv("x");
        assertThatThrownBy(() -> strategy.apply(new CustomerPayment(),c)).isInstanceOf(BadRequestException.class);
        c.setCvv("123"); c.setNumber(null);
        assertThatThrownBy(() -> strategy.apply(new CustomerPayment(),c)).isInstanceOf(BadRequestException.class);
        c.setNumber(card().getNumber()); c.setHolderName(" ");
        assertThatThrownBy(() -> strategy.apply(new CustomerPayment(),c)).isInstanceOf(BadRequestException.class);
    }
}
