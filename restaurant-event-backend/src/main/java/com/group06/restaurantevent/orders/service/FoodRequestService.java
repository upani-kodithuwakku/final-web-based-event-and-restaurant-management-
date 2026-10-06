package com.group06.restaurantevent.orders.service;

import com.group06.restaurantevent.common.enums.FoodRequestStatus;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.menu.entity.MenuItem;
import com.group06.restaurantevent.menu.service.MenuService;
import com.group06.restaurantevent.orders.dto.request.CreateFoodRequestRequest;
import com.group06.restaurantevent.orders.dto.response.FoodRequestResponse;
import com.group06.restaurantevent.orders.entity.FoodRequest;
import com.group06.restaurantevent.orders.repository.FoodRequestRepository;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodRequestService {

    private final FoodRequestRepository requestRepository;
    private final MenuService menuService;
    private final UserRepository userRepository;

    @Transactional
    public FoodRequestResponse create(String customerEmail, CreateFoodRequestRequest req) {
        User customer = findUserByEmail(customerEmail);
        FoodRequest request = new FoodRequest();
        request.setCustomerId(customer.getId());
        request.setCustomerName(customer.getFullName());
        request.setMessage(req.getMessage().trim());

        if (req.getMenuItemId() != null) {
            MenuItem item = menuService.findItem(req.getMenuItemId());
            if (!item.isActive())
                throw new ResourceNotFoundException("Menu item is no longer listed");
            request.setMenuItemId(item.getId());
            request.setItemName(item.getName());
        }
        return toResponse(requestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<FoodRequestResponse> myRequests(String customerEmail) {
        return requestRepository.findByCustomerIdOrderByCreatedAtDesc(findUserByEmail(customerEmail).getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FoodRequestResponse> openQueue() {
        return requestRepository.findByStatusOrderByCreatedAtAsc(FoodRequestStatus.OPEN)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public FoodRequestResponse resolve(Long id, String staffEmail) {
        FoodRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food request not found: " + id));
        // Resolving twice keeps the first staff member and time.
        if (request.getStatus() == FoodRequestStatus.OPEN) {
            request.setStatus(FoodRequestStatus.RESOLVED);
            request.setResolvedBy(findUserByEmail(staffEmail).getId());
            request.setResolvedAt(LocalDateTime.now());
        }
        return toResponse(requestRepository.save(request));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private FoodRequestResponse toResponse(FoodRequest r) {
        return FoodRequestResponse.builder()
                .id(r.getId())
                .customerId(r.getCustomerId())
                .customerName(r.getCustomerName())
                .menuItemId(r.getMenuItemId())
                .itemName(r.getItemName())
                .message(r.getMessage())
                .status(r.getStatus().name())
                .resolvedBy(r.getResolvedBy())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }
}
