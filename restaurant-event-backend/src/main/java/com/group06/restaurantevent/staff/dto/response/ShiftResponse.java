package com.group06.restaurantevent.staff.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class ShiftResponse {
    private Long id;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String roleRequired;
    private int requiredStaffCount;
    private String status;
    private List<ShiftAssignmentResponse> assignments;
}
