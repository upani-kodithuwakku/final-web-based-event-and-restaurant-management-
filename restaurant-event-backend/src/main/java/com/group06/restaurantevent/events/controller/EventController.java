package com.group06.restaurantevent.events.controller;

import com.group06.restaurantevent.events.dto.request.CreateEventBookingRequest;
import com.group06.restaurantevent.events.dto.response.EventBookingResponse;
import com.group06.restaurantevent.events.dto.response.EventHallResponse;
import com.group06.restaurantevent.events.dto.response.EventPackageResponse;
import com.group06.restaurantevent.events.service.EventBookingService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventBookingService service;

    @GetMapping("/halls")
    public ResponseEntity<List<EventHallResponse>> halls() {
        return ResponseEntity.ok(service.listHalls());
    }

    @GetMapping("/packages")
    public ResponseEntity<List<EventPackageResponse>> packages() {
        return ResponseEntity.ok(service.listPackages());
    }

    @PostMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<EventBookingResponse> book(@AuthenticationPrincipal UserDetails user,
                                                     @Valid @RequestBody CreateEventBookingRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createBooking(user.getUsername(), req));
    }

    @GetMapping("/bookings/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<EventBookingResponse>> myBookings(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.myBookings(user.getUsername()));
    }

    @GetMapping("/bookings/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<EventBookingResponse> getBooking(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.getBooking(id, user.getUsername()));
    }

    @PatchMapping("/bookings/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<EventBookingResponse> cancel(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.cancelBooking(id, user.getUsername()));
    }
}
