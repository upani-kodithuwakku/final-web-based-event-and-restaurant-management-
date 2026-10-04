package com.group06.restaurantevent.staff.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class StaffProfileResponse {
    private Long id;
    private Long userId;
    private String employeeCode;
    private String fullName;
    private String email;
    private String phone;
    private String jobTitle;
    private String employmentStatus;
    private LocalDate joinedDate;
    private boolean isActive;
    private Set<String> roles;

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }
}
