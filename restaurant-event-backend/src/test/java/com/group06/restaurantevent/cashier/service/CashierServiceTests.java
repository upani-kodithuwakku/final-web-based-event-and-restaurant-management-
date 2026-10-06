package com.group06.restaurantevent.cashier.service;

import com.group06.restaurantevent.billing.entity.*;
import com.group06.restaurantevent.billing.repository.*;
import com.group06.restaurantevent.payment.entity.CustomerPayment;
import com.group06.restaurantevent.payment.repository.CustomerPaymentRepository;
import com.group06.restaurantevent.common.enums.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CashierServiceTests {
    @Test void combinesBothSourcesByCollectionDateInColombo() {
        var billing = mock(PaymentRepository.class);
        var customer = mock(CustomerPaymentRepository.class);
        var invoices = mock(InvoiceRepository.class);
        // UTC is still Oct 4, but Colombo has crossed midnight into Oct 5.
        var clock = Clock.fixed(Instant.parse("2026-10-04T19:00:00Z"), ZoneOffset.UTC);
        var invoice = Invoice.builder().invoiceType(InvoiceType.FOOD_ORDER).status(InvoiceStatus.ISSUED)
                .totalAmount(new BigDecimal("100")).issuedAt(LocalDateTime.parse("2026-09-01T12:00:00")).build();
        var draft = Invoice.builder().status(InvoiceStatus.DRAFT).totalAmount(new BigDecimal("50")).build();
        var card = Payment.builder().paymentReference("BILL").invoice(invoice).status(PaymentStatus.PAID)
                .amount(new BigDecimal("100")).method(PaymentMethod.CARD).paidAt(LocalDateTime.parse("2026-10-05T00:05:00")).build();
        var past = Payment.builder().paymentReference("OLD").invoice(invoice).status(PaymentStatus.PAID)
                .amount(new BigDecimal("900")).method(PaymentMethod.CASH).paidAt(LocalDateTime.parse("2026-10-04T23:59:59")).build();
        var outlet = CustomerPayment.builder().paymentReference("OUTLET").purpose(PaymentPurpose.EVENT_BOOKING)
                .amount(new BigDecimal("200")).method(PaymentOption.PAY_AT_OUTLET).status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.parse("2026-10-05T00:10:00")).build();
        var pending = CustomerPayment.builder().amount(new BigDecimal("80")).method(PaymentOption.PAY_AT_OUTLET)
                .status(PaymentStatus.PENDING).build();
        var failed = CustomerPayment.builder().amount(new BigDecimal("999")).method(PaymentOption.CARD)
                .status(PaymentStatus.FAILED).paidAt(LocalDateTime.parse("2026-10-05T00:12:00")).build();
        when(billing.findAll()).thenReturn(List.of(card, past));
        when(customer.findAll()).thenReturn(List.of(outlet, pending, failed));
        when(invoices.findAll()).thenReturn(List.of(invoice, draft));
        var summary = new CashierService(billing, customer, invoices, clock).summary();
        assertEquals(new BigDecimal("300"), summary.todayRevenue());
        assertEquals(new BigDecimal("100"), summary.todayCardRevenue());
        assertEquals(new BigDecimal("200"), summary.todayOutletRevenue());
        assertEquals(2, summary.unpaidInvoices().count());
        assertEquals(new BigDecimal("150"), summary.unpaidInvoices().total());
        assertEquals(1, summary.awaitingCollection().count());
        assertEquals(new BigDecimal("80"), summary.awaitingCollection().total());
        assertEquals(List.of("OUTLET", "BILL", "OLD"), summary.recentPayments().stream().map(p -> p.reference()).toList());
    }

    @Test void recentCollectionsAreLimitedToTenAndMissingDatesAreExcluded() {
        var billing = mock(PaymentRepository.class); var customer = mock(CustomerPaymentRepository.class);
        var invoices = mock(InvoiceRepository.class);
        var entries = new ArrayList<CustomerPayment>();
        for (int i = 0; i < 12; i++) entries.add(CustomerPayment.builder().paymentReference("P" + i)
                .purpose(PaymentPurpose.FOOD_ORDER).method(PaymentOption.CARD).status(PaymentStatus.PAID)
                .amount(BigDecimal.ONE).paidAt(LocalDateTime.parse("2026-10-05T10:00:00").plusMinutes(i)).build());
        entries.add(CustomerPayment.builder().status(PaymentStatus.PAID).build());
        when(billing.findAll()).thenReturn(List.of()); when(customer.findAll()).thenReturn(entries); when(invoices.findAll()).thenReturn(List.of());
        var summary = new CashierService(billing, customer, invoices, Clock.fixed(Instant.parse("2026-10-05T12:00:00Z"), ZoneOffset.UTC)).summary();
        assertEquals(10, summary.recentPayments().size()); assertEquals("P11", summary.recentPayments().getFirst().reference());
        assertEquals(new BigDecimal("12"), summary.todayRevenue());
    }
}
