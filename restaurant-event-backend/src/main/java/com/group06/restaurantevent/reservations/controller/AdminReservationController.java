package com.group06.restaurantevent.reservations.controller;

import com.group06.restaurantevent.common.enums.ReservationStatus;
import com.group06.restaurantevent.common.response.ApiResponse;
import com.group06.restaurantevent.reservations.dto.request.CreateTableRequest;
import com.group06.restaurantevent.reservations.dto.request.UpdateTableStatusRequest;
import com.group06.restaurantevent.reservations.dto.response.ReservationResponse;
import com.group06.restaurantevent.reservations.dto.response.TableResponse;
import com.group06.restaurantevent.reservations.service.ReservationService;
import com.group06.restaurantevent.reservations.service.TableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Tables & Reservations")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER','WAITER')")
public class AdminReservationController {

    private final TableService tableService;
    private final ReservationService reservationService;

    // ---------- Table Management ----------

    @GetMapping("/tables")
    @Operation(summary = "List all tables")
    public ResponseEntity<ApiResponse<List<TableResponse>>> getTables() {
        return ResponseEntity.ok(ApiResponse.success(tableService.getAllTables()));
    }

    @PostMapping("/tables")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Create a table")
    public ResponseEntity<ApiResponse<TableResponse>> createTable(@Valid @RequestBody CreateTableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Table created", tableService.createTable(request)));
    }

    @PutMapping("/tables/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Update a table")
    public ResponseEntity<ApiResponse<TableResponse>> updateTable(
            @PathVariable Long id, @Valid @RequestBody CreateTableRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Table updated", tableService.updateTable(id, request)));
    }

    @PatchMapping("/tables/{id}/status")
    @Operation(summary = "Update table status")
    public ResponseEntity<ApiResponse<TableResponse>> updateTableStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateTableStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", tableService.updateTableStatus(id, request)));
    }

    @DeleteMapping("/tables/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Deactivate a table")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.ok(ApiResponse.success("Table deactivated", null));
    }

    // ---------- Reservation Management ----------

    @GetMapping("/reservations")
    @Operation(summary = "Get reservations by date and status")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ReservationStatus status) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                reservationService.getReservationsByDate(queryDate, status)));
    }

    @PatchMapping("/reservations/{id}/check-in")
    @Operation(summary = "Check in a customer")
    public ResponseEntity<ApiResponse<ReservationResponse>> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Checked in", reservationService.checkIn(id)));
    }

    @PatchMapping("/reservations/{id}/complete")
    @Operation(summary = "Complete a reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Completed", reservationService.complete(id)));
    }

    @PatchMapping("/reservations/{id}/no-show")
    @Operation(summary = "Mark as no-show")
    public ResponseEntity<ApiResponse<ReservationResponse>> noShow(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Marked no-show", reservationService.markNoShow(id)));
    }
}
