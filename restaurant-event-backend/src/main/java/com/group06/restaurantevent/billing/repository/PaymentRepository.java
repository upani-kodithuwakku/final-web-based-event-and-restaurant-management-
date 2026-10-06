package com.group06.restaurantevent.billing.repository;

import com.group06.restaurantevent.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceIdOrderByCreatedAtDesc(Long invoiceId);
}
