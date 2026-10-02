package com.group06.restaurantevent.orders.repository;

import com.group06.restaurantevent.common.enums.OrderStatus;
import com.group06.restaurantevent.orders.entity.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<FoodOrder> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);
    List<FoodOrder> findAllByOrderByCreatedAtDesc();
}
