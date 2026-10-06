package com.group06.restaurantevent.billing.repository;

import com.group06.restaurantevent.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<Invoice> findByFoodOrderId(Long foodOrderId);
    Optional<Invoice> findByEventBookingId(Long eventBookingId);
}
