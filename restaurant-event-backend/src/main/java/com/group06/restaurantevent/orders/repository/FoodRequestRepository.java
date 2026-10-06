package com.group06.restaurantevent.orders.repository;

import com.group06.restaurantevent.common.enums.FoodRequestStatus;
import com.group06.restaurantevent.orders.entity.FoodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodRequestRepository extends JpaRepository<FoodRequest, Long> {
    List<FoodRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<FoodRequest> findByStatusOrderByCreatedAtAsc(FoodRequestStatus status);
}
