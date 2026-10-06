package com.group06.restaurantevent.cashier.service;

import com.group06.restaurantevent.billing.repository.InvoiceRepository;
import com.group06.restaurantevent.billing.repository.PaymentRepository;
import com.group06.restaurantevent.payment.repository.CustomerPaymentRepository;
import com.group06.restaurantevent.cashier.dto.response.CashierSummaryResponse;
import com.group06.restaurantevent.cashier.dto.response.CashierSummaryResponse.*;
import com.group06.restaurantevent.common.enums.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class CashierService {
    private final PaymentRepository payments;
    private final CustomerPaymentRepository customerPayments;
    private final InvoiceRepository invoices;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public CashierService(PaymentRepository payments, CustomerPaymentRepository customerPayments, InvoiceRepository invoices) {
        this(payments, customerPayments, invoices, Clock.system(ZoneId.of("Asia/Colombo")));
    }

    CashierService(PaymentRepository payments, CustomerPaymentRepository customerPayments, InvoiceRepository invoices, Clock clock) {
        this.payments = payments; this.customerPayments = customerPayments; this.invoices = invoices; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CashierSummaryResponse summary() {
        LocalDate today = LocalDate.now(clock.withZone(ZoneId.of("Asia/Colombo")));
        List<RecentPayment> collected = new ArrayList<>();
        payments.findAll().stream().filter(p -> p.getStatus() == PaymentStatus.PAID && p.getPaidAt() != null)
                .forEach(p -> collected.add(new RecentPayment(p.getPaymentReference(), p.getInvoice().getInvoiceType().name(), p.getAmount(), p.getMethod().name(), p.getPaidAt())));
        var customer = customerPayments.findAll();
        customer.stream().filter(p -> p.getStatus() == PaymentStatus.PAID && p.getPaidAt() != null)
                .forEach(p -> collected.add(new RecentPayment(p.getPaymentReference(), p.getPurpose().name(), p.getAmount(), p.getMethod().name(), p.getPaidAt())));
        var todays = collected.stream().filter(p -> p.paidAt().toLocalDate().equals(today)).toList();
        BigDecimal total = todays.stream().map(RecentPayment::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outlet = todays.stream().filter(p -> p.method().equals("CASH") || p.method().equals("PAY_AT_OUTLET"))
                .map(RecentPayment::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        var unpaid = invoices.findAll().stream().filter(i -> i.getStatus() == InvoiceStatus.DRAFT || i.getStatus() == InvoiceStatus.ISSUED).toList();
        var pending = customer.stream().filter(p -> p.getMethod() == PaymentOption.PAY_AT_OUTLET && p.getStatus() == PaymentStatus.PENDING).toList();
        return new CashierSummaryResponse(total, total.subtract(outlet), outlet,
                new Balance(unpaid.size(), unpaid.stream().map(i -> i.getTotalAmount()).reduce(BigDecimal.ZERO, BigDecimal::add)),
                new Balance(pending.size(), pending.stream().map(p -> p.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add)),
                collected.stream().sorted(Comparator.comparing(RecentPayment::paidAt).reversed()).limit(10).toList());
    }
}
