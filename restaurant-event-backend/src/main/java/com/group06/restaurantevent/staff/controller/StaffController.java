package com.group06.restaurantevent.staff.controller;

import com.group06.restaurantevent.staff.dto.request.CreateShiftRequest;
import com.group06.restaurantevent.staff.dto.request.CreateStaffProfileRequest;
import com.group06.restaurantevent.staff.dto.request.CreateStaffUserRequest;
import com.group06.restaurantevent.staff.dto.response.ShiftAssignmentResponse;
import com.group06.restaurantevent.staff.dto.response.ShiftResponse;
import com.group06.restaurantevent.staff.dto.response.StaffProfileResponse;
import com.group06.restaurantevent.staff.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class StaffController {

    private final StaffService staffService;

    // ---- Staff user management ----

    /** Create a full staff account (User + roles + StaffProfile) in one call */
    @PostMapping("/users")
    public ResponseEntity<StaffProfileResponse> createStaffUser(
            @Valid @RequestBody CreateStaffUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.createStaffUser(req));
    }

    @GetMapping
    public ResponseEntity<List<StaffProfileResponse>> list() {
        return ResponseEntity.ok(staffService.listStaff());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StaffProfileResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getStaff(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StaffProfileResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody CreateStaffProfileRequest req) {
        return ResponseEntity.ok(staffService.updateProfile(id, req));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id,
                                             @RequestBody Map<String, Boolean> body) {
        staffService.toggleActive(id, body.getOrDefault("active", true));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/roles")
    public ResponseEntity<StaffProfileResponse> updateRoles(@PathVariable Long id,
                                                            @RequestBody Map<String, Set<String>> body) {
        return ResponseEntity.ok(staffService.updateRoles(id, body.get("roles")));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        staffService.resetPassword(id, body.get("password"));
        return ResponseEntity.noContent().build();
    }

    // ---- Shift management ----

    @GetMapping("/shifts")
    public ResponseEntity<List<ShiftResponse>> shifts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(staffService.listShifts(date));
    }

    @PostMapping("/shifts")
    public ResponseEntity<ShiftResponse> createShift(@Valid @RequestBody CreateShiftRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffService.createShift(req));
    }

    @DeleteMapping("/shifts/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        staffService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shifts/{shiftId}/assignments")
    public ResponseEntity<List<ShiftAssignmentResponse>> listAssignments(@PathVariable Long shiftId) {
        return ResponseEntity.ok(staffService.listAssignments(shiftId));
    }

    @PostMapping("/shifts/{shiftId}/assign")
    public ResponseEntity<ShiftAssignmentResponse> assign(@PathVariable Long shiftId,
                                                          @RequestBody Map<String, Long> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(staffService.assignStaff(shiftId, body.get("staffId")));
    }

    @DeleteMapping("/shifts/{shiftId}/assignments/{staffId}")
    public ResponseEntity<Void> unassign(@PathVariable Long shiftId, @PathVariable Long staffId) {
        staffService.unassignStaff(shiftId, staffId);
        return ResponseEntity.noContent().build();
    }
}
