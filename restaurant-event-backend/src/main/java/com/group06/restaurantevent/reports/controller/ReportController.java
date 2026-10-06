package com.group06.restaurantevent.reports.controller;

import com.group06.restaurantevent.reports.dto.response.*;
import com.group06.restaurantevent.reports.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
@Tag(name = "Reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Today's snapshot")
    public ResponseEntity<DashboardReportResponse> dashboard() {
        return ResponseEntity.ok(reportService.dashboard());
    }

    @GetMapping("/reservations")
    @Operation(summary = "Reservation report (default: last 30 days)")
    public ResponseEntity<ReservationReportResponse> reservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        return ResponseEntity.ok(reportService.reservations(from != null ? from : end.minusDays(29), end));
    }

    @GetMapping("/sales")
    @Operation(summary = "Food sales report (default: today)")
    public ResponseEntity<SalesReportResponse> sales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        return ResponseEntity.ok(reportService.sales(from != null ? from : end, end));
    }

    @GetMapping("/inventory")
    @Operation(summary = "Low-stock inventory report")
    public ResponseEntity<InventoryReportResponse> inventory() {
        return ResponseEntity.ok(reportService.inventory());
    }

    @GetMapping("/events")
    @Operation(summary = "Event bookings received (default: last 30 days)")
    public ResponseEntity<EventReportResponse> events(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(reportService.events(from != null ? from : today.minusDays(30), to != null ? to : today));
    }
}
