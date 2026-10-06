package com.group06.restaurantevent.payment.controller;

import com.group06.restaurantevent.payment.dto.request.CreateCustomerPaymentRequest;
import com.group06.restaurantevent.payment.dto.request.UpdateCustomerPaymentRequest;
import com.group06.restaurantevent.payment.dto.request.UpdatePaymentStatusRequest;
import com.group06.restaurantevent.payment.dto.response.CustomerPaymentResponse;
import com.group06.restaurantevent.payment.dto.response.PaymentSummaryResponse;
import com.group06.restaurantevent.payment.service.CustomerPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
@SecurityRequirement(name = "bearerAuth")
public class CustomerPaymentController {

    private static final Set<String> STAFF_AUTHORITIES = Set.of("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_CASHIER");

    private final CustomerPaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Pay for a confirmed food order or event booking")
    public ResponseEntity<CustomerPaymentResponse> create(@AuthenticationPrincipal UserDetails principal,
                                                          @Valid @RequestBody CreateCustomerPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.create(principal.getUsername(), req));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "List my payments")
    public ResponseEntity<List<CustomerPaymentResponse>> myPayments(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(paymentService.myPayments(principal.getUsername()));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "My bill: food total, event total, and full total")
    public ResponseEntity<PaymentSummaryResponse> summary(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(paymentService.summary(principal.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "List all customer payments (staff)")
    public ResponseEntity<List<CustomerPaymentResponse>> all(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(paymentService.allPayments(status));
    }

    @GetMapping("/reservations")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','WAITER')")
    public ResponseEntity<List<CustomerPaymentResponse>> reservationPayments() {
        return ResponseEntity.ok(paymentService.paymentsForPurpose(com.group06.restaurantevent.common.enums.PaymentPurpose.TABLE_RESERVATION));
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EVENT_COORDINATOR')")
    public ResponseEntity<List<CustomerPaymentResponse>> eventPayments() {
        return ResponseEntity.ok(paymentService.paymentsForPurpose(com.group06.restaurantevent.common.enums.PaymentPurpose.EVENT_BOOKING));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "Get one payment (customers see only their own)")
    public ResponseEntity<CustomerPaymentResponse> get(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(paymentService.getPayment(id, principal.getUsername(), isStaff(principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Change how I pay, before the payment is completed")
    public ResponseEntity<CustomerPaymentResponse> updateMethod(@PathVariable Long id,
                                                                @AuthenticationPrincipal UserDetails principal,
                                                                @Valid @RequestBody UpdateCustomerPaymentRequest req) {
        return ResponseEntity.ok(paymentService.updateMethod(id, principal.getUsername(), req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','CASHIER')")
    @Operation(summary = "Update payment status, e.g. mark a pay-at-outlet payment as paid (staff)")
    public ResponseEntity<CustomerPaymentResponse> updateStatus(@PathVariable Long id,
                                                                @AuthenticationPrincipal UserDetails principal,
                                                                @Valid @RequestBody UpdatePaymentStatusRequest req) {
        return ResponseEntity.ok(paymentService.updateStatus(id, principal.getUsername(), req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a payment (admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        paymentService.delete(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    private boolean isStaff(UserDetails principal) {
        return principal.getAuthorities().stream().anyMatch(a -> STAFF_AUTHORITIES.contains(a.getAuthority()));
    }
}
