package com.group06.restaurantevent.billing.controller;

import com.group06.restaurantevent.billing.dto.request.CreateInvoiceRequest;
import com.group06.restaurantevent.billing.dto.request.CreatePaymentRequest;
import com.group06.restaurantevent.billing.dto.response.InvoiceResponse;
import com.group06.restaurantevent.billing.dto.response.PaymentResponse;
import com.group06.restaurantevent.billing.service.BillingService;
import com.group06.restaurantevent.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN','MANAGER')")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.createInvoice(req));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN','MANAGER')")
    public ResponseEntity<List<InvoiceResponse>> allInvoices() {
        return ResponseEntity.ok(billingService.allInvoices());
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getInvoice(id));
    }

    @GetMapping("/invoices/my")
    public ResponseEntity<List<InvoiceResponse>> myInvoices(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(billingService.myInvoices(user.getId()));
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN','MANAGER')")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody CreatePaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.processPayment(req));
    }

    @GetMapping("/payments/{invoiceId}")
    public ResponseEntity<List<PaymentResponse>> getPayments(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(billingService.getPaymentsForInvoice(invoiceId));
    }
}
