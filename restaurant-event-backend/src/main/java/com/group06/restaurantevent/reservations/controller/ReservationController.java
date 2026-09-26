package com.group06.restaurantevent.reservations.controller;

import com.group06.restaurantevent.common.response.ApiResponse;
import com.group06.restaurantevent.reservations.dto.request.CancelReservationRequest;
import com.group06.restaurantevent.reservations.dto.request.CreateReservationRequest;
import com.group06.restaurantevent.reservations.dto.request.UpdateReservationRequest;
import com.group06.restaurantevent.reservations.dto.response.AvailabilityResponse;
import com.group06.restaurantevent.reservations.dto.response.ReservationResponse;
import com.group06.restaurantevent.reservations.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/availability")
    @Operation(summary = "Check table availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam int guests,
            @RequestParam(required = false) String preference) {
        return ResponseEntity.ok(ApiResponse.success(
                reservationService.checkAvailability(date, time, guests, preference)));
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(principal.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Reservation created", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my reservations")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                reservationService.getMyReservations(principal.getUsername())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get single reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                reservationService.getReservationForCustomer(id, principal.getUsername())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update a reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateReservationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reservation updated",
                reservationService.updateReservation(id, principal.getUsername(), request)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel a reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody(required = false) CancelReservationRequest request) {
        if (request == null) request = new CancelReservationRequest();
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled",
                reservationService.cancelReservation(id, principal.getUsername(), request)));
    }
}
