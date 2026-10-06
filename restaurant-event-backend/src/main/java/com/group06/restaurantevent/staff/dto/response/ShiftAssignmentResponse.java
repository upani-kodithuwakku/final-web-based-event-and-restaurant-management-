package com.group06.restaurantevent.staff.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftAssignmentResponse {
    private Long id;
    private Long shiftId;
    private Long staffId;
    private String assignedRole;
    private String status;
}
