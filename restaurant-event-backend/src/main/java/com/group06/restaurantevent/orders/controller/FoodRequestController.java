package com.group06.restaurantevent.orders.controller;

import com.group06.restaurantevent.orders.dto.request.CreateFoodRequestRequest;
import com.group06.restaurantevent.orders.dto.response.FoodRequestResponse;
import com.group06.restaurantevent.orders.service.FoodRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food-requests")
@RequiredArgsConstructor
public class FoodRequestController {

    private final FoodRequestService foodRequestService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<FoodRequestResponse> create(@AuthenticationPrincipal UserDetails principal,
                                                      @Valid @RequestBody CreateFoodRequestRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodRequestService.create(principal.getUsername(), req));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<FoodRequestResponse>> myRequests(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(foodRequestService.myRequests(principal.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','WAITER','KITCHEN_STAFF')")
    public ResponseEntity<List<FoodRequestResponse>> queue() {
        return ResponseEntity.ok(foodRequestService.openQueue());
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','WAITER','KITCHEN_STAFF')")
    public ResponseEntity<FoodRequestResponse> resolve(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(foodRequestService.resolve(id, principal.getUsername()));
    }
}
