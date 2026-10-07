package com.group06.restaurantevent.staff.service;

import com.group06.restaurantevent.common.enums.*;
import com.group06.restaurantevent.common.exception.*;
import com.group06.restaurantevent.staff.dto.request.AttendanceRequest;
import com.group06.restaurantevent.staff.entity.*;
import com.group06.restaurantevent.staff.repository.*;
import com.group06.restaurantevent.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {
    private final AttendanceRepository attendance;
    private final ShiftAssignmentRepository assignments;
    private final StaffProfileRepository profiles;
    private final UserRepository users;

    public record Response(
            Long id,
            Long assignmentId,
            Long staffId,
            String staffName,
            LocalDate shiftDate,
            String status,
            LocalDateTime checkInAt,
            LocalDateTime checkOutAt) {}

    @Transactional(readOnly = true)
    public List<Response> list() {
        return attendance.findAll().stream().map(this::response).toList();
    }

    public Response save(Long id, AttendanceRequest r) {
        var assignment =
                assignments
                        .findById(r.assignmentId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Shift assignment not found"));
        var shift = assignment.getShift();
        if (shift.getStatus() == ShiftStatus.CANCELLED)
            throw new BadRequestException("Cannot record attendance for a cancelled shift");
        if (shift.getShiftDate()
                .atTime(shift.getStartTime())
                .isAfter(LocalDateTime.now(ZoneId.of("Asia/Colombo"))))
            throw new BadRequestException("Attendance can only be recorded after the shift starts");
        var row =
                id == null
                        ? new AttendanceRecord()
                        : attendance
                                .findById(id)
                                .orElseThrow(
                                        () ->
                                                new ResourceNotFoundException(
                                                        "Attendance not found"));
        if (id != null && !row.getAssignment().getId().equals(r.assignmentId()))
            throw new BadRequestException("Attendance assignment cannot be changed");
        if (id == null && attendance.existsByAssignment_Id(r.assignmentId()))
            throw new ConflictException("Attendance already exists for this assignment");
        if (r.status() == AttendanceStatus.ABSENT
                && (r.checkInAt() != null || r.checkOutAt() != null))
            throw new BadRequestException("Absent staff cannot have check-in or check-out times");
        if (r.status() != AttendanceStatus.ABSENT && r.checkInAt() == null)
            throw new BadRequestException("Check-in time is required");
        if (r.checkInAt() != null && !r.checkInAt().toLocalDate().equals(shift.getShiftDate()))
            throw new BadRequestException("Check-in date must match the shift date");
        if (r.checkOutAt() != null
                && (r.checkInAt() == null
                        || !r.checkOutAt().isAfter(r.checkInAt())
                        || !r.checkOutAt().toLocalDate().equals(shift.getShiftDate())))
            throw new BadRequestException("Check-out must be after check-in on the shift date");
        row.setAssignment(assignment);
        row.setStatus(r.status());
        row.setCheckInAt(r.checkInAt());
        row.setCheckOutAt(r.checkOutAt());
        return response(attendance.save(row));
    }

    public void delete(Long id) {
        var row =
                attendance
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        attendance.delete(row);
    }

    private Response response(AttendanceRecord a) {
        var assignment = a.getAssignment();
        var profile =
                profiles.findById(assignment.getStaffId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Staff profile not found"));
        return new Response(
                a.getId(),
                assignment.getId(),
                profile.getId(),
                users.findById(profile.getUserId()).map(u -> u.getFullName()).orElse("Staff"),
                assignment.getShift().getShiftDate(),
                a.getStatus().name(),
                a.getCheckInAt(),
                a.getCheckOutAt());
    }
}
