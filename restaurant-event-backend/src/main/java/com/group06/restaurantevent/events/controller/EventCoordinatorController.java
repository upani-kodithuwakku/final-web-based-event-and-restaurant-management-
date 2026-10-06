package com.group06.restaurantevent.events.controller;

import com.group06.restaurantevent.events.dto.response.EventBookingResponse;
import com.group06.restaurantevent.events.service.EventBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/event-coordinator")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EVENT_COORDINATOR','ADMIN','MANAGER')")
public class EventCoordinatorController {

    private final EventBookingService service;

    @GetMapping("/bookings")
    public ResponseEntity<List<EventBookingResponse>> all() {
        return ResponseEntity.ok(service.allBookings());
    }

    @PatchMapping("/bookings/{id}/approve")
    public ResponseEntity<EventBookingResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approveBooking(id));
    }

    @PatchMapping("/bookings/{id}/reject")
    public ResponseEntity<EventBookingResponse> reject(@PathVariable Long id,
                                                       @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.rejectBooking(id, body));
    }
}
