package com.group06.restaurantevent.payment.repository;

import com.group06.restaurantevent.common.enums.PaymentStatus;
import com.group06.restaurantevent.payment.entity.CustomerPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    List<CustomerPayment> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<CustomerPayment> findAllByOrderByCreatedAtDesc();
    List<CustomerPayment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
    Optional<CustomerPayment> findByFoodOrderId(Long foodOrderId);
    Optional<CustomerPayment> findByEventBookingId(Long eventBookingId);
    Optional<CustomerPayment> findByTableReservationId(Long tableReservationId);
    boolean existsByTableReservationId(Long tableReservationId);
    boolean existsByFoodOrderId(Long foodOrderId);
    boolean existsByEventBookingId(Long eventBookingId);
}
