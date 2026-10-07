package com.group06.restaurantevent.staff.service;

import com.group06.restaurantevent.common.enums.AssignmentStatus;
import com.group06.restaurantevent.common.enums.EmploymentStatus;
import com.group06.restaurantevent.common.enums.ShiftStatus;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ConflictException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.staff.dto.request.CreateShiftRequest;
import com.group06.restaurantevent.staff.dto.request.CreateStaffProfileRequest;
import com.group06.restaurantevent.staff.dto.request.CreateStaffUserRequest;
import com.group06.restaurantevent.staff.dto.response.ShiftAssignmentResponse;
import com.group06.restaurantevent.staff.dto.response.ShiftResponse;
import com.group06.restaurantevent.staff.dto.response.StaffProfileResponse;
import com.group06.restaurantevent.staff.entity.Shift;
import com.group06.restaurantevent.staff.entity.ShiftAssignment;
import com.group06.restaurantevent.staff.entity.StaffProfile;
import com.group06.restaurantevent.staff.repository.ShiftAssignmentRepository;
import com.group06.restaurantevent.staff.repository.ShiftRepository;
import com.group06.restaurantevent.staff.repository.StaffProfileRepository;
import com.group06.restaurantevent.users.entity.Role;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.RoleRepository;
import com.group06.restaurantevent.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffProfileRepository profileRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.group06.restaurantevent.staff.repository.AttendanceRepository attendance;

    // ---- Staff user creation (P2) ----

    @Transactional
    public StaffProfileResponse createStaffUser(CreateStaffUserRequest req) {
        req.setEmail(req.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
        req.setFullName(req.getFullName().trim());
        Set<String> selectedRoles = normalizeStaffRoles(req.getRoles());
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already registered: " + req.getEmail());
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : selectedRoles) {
            Role role = roleRepository.findByName(roleName.toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
            roles.add(role);
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .isActive(true)
                .roles(roles)
                .build();
        user = userRepository.save(user);

        StaffProfile profile = StaffProfile.builder()
                .userId(user.getId())
                .employeeCode(generateEmpCode())
                .jobTitle(req.getJobTitle())
                .employmentStatus(parseStatus(req.getEmploymentStatus()))
                .joinedDate(req.getJoinedDate() != null ? req.getJoinedDate() : LocalDate.now())
                .isActive(true)
                .build();
        return toProfileResponse(profileRepository.save(profile), user);
    }

    // ---- Staff listing — all non-CUSTOMER users from DB ----

    @Transactional
    public List<StaffProfileResponse> listStaff() {
        return userRepository.findAllExcludingRole("CUSTOMER")
                .stream().map(u -> {
                    StaffProfile p = ensureProfile(u);
                    return toUserResponse(u, p);
                }).toList();
    }

    @Transactional(readOnly = true)
    public StaffProfileResponse getStaff(Long id) {
        StaffProfile p = findProfile(id);
        return toProfileResponse(p, userRepository.findById(p.getUserId()).orElse(null));
    }

    // ---- Profile update ----

    @Transactional
    public StaffProfileResponse updateProfile(Long id, CreateStaffProfileRequest req) {
        StaffProfile p = findProfile(id);
        p.setJobTitle(req.getJobTitle());
        p.setEmploymentStatus(parseStatus(req.getEmploymentStatus()));
        profileRepository.save(p);
        User u = userRepository.findById(p.getUserId()).orElse(null);
        return toProfileResponse(p, u);
    }

    @Transactional
    public void toggleActive(Long id, boolean active) {
        StaffProfile p = findProfile(id);
        if (active && p.getEmploymentStatus() == EmploymentStatus.TERMINATED)
            throw new BadRequestException("Update the employment status before reactivating terminated staff");
        p.setActive(active);
        userRepository.findById(p.getUserId()).ifPresent(u -> {
            u.setActive(active);
            userRepository.save(u);
        });
        profileRepository.save(p);
    }

    // ---- Role management (P10) ----

    @Transactional
    public StaffProfileResponse updateRoles(Long id, Set<String> roleNames) {
        StaffProfile p = findProfile(id);
        User u = userRepository.findById(p.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for staff profile"));
        Set<Role> roles = normalizeStaffRoles(roleNames).stream()
                .map(n -> roleRepository.findByName(n.toUpperCase())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + n)))
                .collect(Collectors.toSet());
        u.setRoles(roles);
        userRepository.save(u);
        return toProfileResponse(p, u);
    }

    // ---- Password reset (P10) ----

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        StaffProfile p = findProfile(id);
        User u = userRepository.findById(p.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for staff profile"));
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 8 || newPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new BadRequestException("Password must be between 8 and 72 characters");
        u.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(u);
    }

    // ---- Shifts ----

    public List<ShiftResponse> listShifts(LocalDate date) {
        List<Shift> shifts = date != null
                ? shiftRepository.findByShiftDateOrderByStartTimeAsc(date)
                : shiftRepository.findAllByOrderByShiftDateAscStartTimeAsc();
        return shifts.stream().map(this::toShiftResponse).toList();
    }

    @Transactional
    public ShiftResponse createShift(CreateShiftRequest req) {
        validateShift(req);
        Shift shift = Shift.builder()
                .shiftDate(req.getShiftDate())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .roleRequired(req.getRoleRequired())
                .requiredStaffCount(req.getRequiredStaffCount())
                .status(ShiftStatus.SCHEDULED)
                .build();
        return toShiftResponse(shiftRepository.save(shift));
    }

    @Transactional
    public void deleteShift(Long id) {
        Shift shift = findShift(id);
        if (shift.getStatus() != ShiftStatus.SCHEDULED)
            throw new BadRequestException("Only SCHEDULED shifts can be deleted");
        if (assignmentRepository.findByShift_Id(id).stream().anyMatch(a -> attendance.existsByAssignment_Id(a.getId())))
            throw new ConflictException("Shifts with attendance cannot be cancelled");
        shift.setStatus(ShiftStatus.CANCELLED);
        shiftRepository.save(shift);
    }

    public List<ShiftAssignmentResponse> listAssignments(Long shiftId) {
        return assignmentRepository.findByShift_Id(shiftId)
                .stream().map(this::toAssignmentResponse).toList();
    }

    @Transactional
    public ShiftAssignmentResponse assignStaff(Long shiftId, Long staffId) {
        Shift shift = findShift(shiftId);
        if (staffId == null) throw new BadRequestException("Select a staff member");
        StaffProfile profile = profileRepository.findLockedById(staffId).orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));
        if (!profile.isActive() || profile.getEmploymentStatus() == EmploymentStatus.TERMINATED) throw new BadRequestException("Inactive or terminated staff cannot be assigned");
        if (shift.getStatus() != ShiftStatus.SCHEDULED) throw new BadRequestException("Only scheduled shifts accept assignments");
        User user = userRepository.findById(profile.getUserId()).orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));
        if (!user.isActive() || user.getRoles().stream().noneMatch(r -> r.getName().equals(shift.getRoleRequired()))) throw new BadRequestException("Staff member must have the required role");

        if (!assignmentRepository.findOverlapping(staffId, shift.getShiftDate(),
                shift.getStartTime(), shift.getEndTime()).isEmpty())
            throw new ConflictException("Staff member already has an overlapping shift on this date");

        if (assignmentRepository.findByShift_IdAndStaffId(shiftId, staffId).isPresent())
            throw new ConflictException("Staff already assigned to this shift");

        if (assignmentRepository.findByShift_Id(shiftId).size() >= shift.getRequiredStaffCount())
            throw new ConflictException("This shift already has the required number of staff");
        ShiftAssignment assignment = ShiftAssignment.builder()
                .shift(shift)
                .staffId(staffId)
                .assignedRole(shift.getRoleRequired())
                .status(AssignmentStatus.ASSIGNED)
                .build();
        return toAssignmentResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public void unassignStaff(Long shiftId, Long staffId) {
        ShiftAssignment assignment = assignmentRepository.findByShift_IdAndStaffId(shiftId, staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        if (attendance.existsByAssignment_Id(assignment.getId())) throw new ConflictException("Keep assignments with recorded attendance for history");
        assignmentRepository.delete(assignment);
    }

    @Transactional
    public ShiftResponse updateShift(Long id, CreateShiftRequest req) {
        validateShift(req);
        Shift shift = findShift(id);
        if(shift.getStatus() != ShiftStatus.SCHEDULED) throw new BadRequestException("Only scheduled shifts can be edited");
        for(ShiftAssignment a: assignmentRepository.findByShift_Id(id)) {
            if(!shift.getRoleRequired().equals(req.getRoleRequired())) throw new ConflictException("Unassign staff before changing the required role");
            if(assignmentRepository.findOverlapping(a.getStaffId(), req.getShiftDate(), req.getStartTime(), req.getEndTime()).stream().anyMatch(x -> !x.getShift().getId().equals(id))) throw new ConflictException("Updated shift overlaps another assignment");
        }
        if (assignmentRepository.findByShift_Id(id).size() > req.getRequiredStaffCount())
            throw new ConflictException("Unassign staff before reducing the required staff count");
        shift.setShiftDate(req.getShiftDate());shift.setStartTime(req.getStartTime());shift.setEndTime(req.getEndTime());shift.setRoleRequired(req.getRoleRequired());shift.setRequiredStaffCount(req.getRequiredStaffCount());
        return toShiftResponse(shiftRepository.save(shift));
    }
    @Transactional
    public void deleteStaff(Long id) {
        StaffProfile p=findProfile(id);
        if(assignmentRepository.findByStaffIdOrderByCreatedAtDesc(id).stream().anyMatch(a -> a.getShift().getStatus()==ShiftStatus.SCHEDULED && !a.getShift().getShiftDate().isBefore(LocalDate.now()))) throw new ConflictException("Unassign upcoming shifts before removing staff");
        p.setEmploymentStatus(EmploymentStatus.TERMINATED);p.setActive(false);profileRepository.save(p);
        userRepository.findById(p.getUserId()).ifPresent(u -> {u.setActive(false); userRepository.save(u);});
    }
    private void validateShift(CreateShiftRequest req) {
        if(!req.getEndTime().isAfter(req.getStartTime())) throw new BadRequestException("Shift end time must be after start time");
        if(req.getShiftDate().atTime(req.getStartTime()).isBefore(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Colombo")))) throw new BadRequestException("Shift start must be in the future");
        if(roleRepository.findByName(req.getRoleRequired()).isEmpty() || "CUSTOMER".equals(req.getRoleRequired())) throw new BadRequestException("Select a valid staff role");
    }

    // ---- Helpers ----

    private Set<String> normalizeStaffRoles(Set<String> values) {
        if (values == null || values.isEmpty()) throw new BadRequestException("Select at least one staff role");
        Set<String> normalized = new HashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) throw new BadRequestException("Staff role cannot be blank");
            String role = value.trim().toUpperCase(java.util.Locale.ROOT);
            if (role.equals("CUSTOMER")) throw new BadRequestException("Staff accounts must use staff roles");
            normalized.add(role);
        }
        return normalized;
    }

    private StaffProfile ensureProfile(User user) {
        return profileRepository.findByUserId(user.getId()).orElseGet(() -> profileRepository.save(
                StaffProfile.builder().userId(user.getId()).employeeCode(generateEmpCode())
                .jobTitle("Staff").employmentStatus(EmploymentStatus.FULL_TIME)
                .joinedDate(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Colombo")))
                .isActive(user.isActive()).build()));
    }

    private StaffProfile findProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found: " + id));
    }

    private Shift findShift(Long id) {
        return shiftRepository.findLockedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id));
    }

    private String generateEmpCode() {
        String suffix = String.format("%04d", new Random().nextInt(9999));
        String code = "EMP-" + suffix;
        return profileRepository.existsByEmployeeCode(code) ? generateEmpCode() : code;
    }

    private EmploymentStatus parseStatus(String s) {
        try { return EmploymentStatus.valueOf(s.toUpperCase()); }
        catch (Exception e) { throw new BadRequestException("Invalid employment status"); }
    }

    private StaffProfileResponse toProfileResponse(StaffProfile p, User u) {
        Set<String> roles = u != null
                ? u.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                : Set.of();
        return StaffProfileResponse.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .employeeCode(p.getEmployeeCode())
                .fullName(u != null ? u.getFullName() : null)
                .email(u != null ? u.getEmail() : null)
                .phone(u != null ? u.getPhone() : null)
                .jobTitle(p.getJobTitle())
                .employmentStatus(p.getEmploymentStatus().name())
                .joinedDate(p.getJoinedDate())
                .isActive(p.isActive())
                .roles(roles)
                .build();
    }

    private StaffProfileResponse toUserResponse(User u, StaffProfile p) {
        Set<String> roles = u.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return StaffProfileResponse.builder()
                .id(p != null ? p.getId() : u.getId())
                .userId(u.getId())
                .employeeCode(p != null ? p.getEmployeeCode() : "—")
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .jobTitle(p != null ? p.getJobTitle() : null)
                .employmentStatus(p != null ? p.getEmploymentStatus().name() : "FULL_TIME")
                .joinedDate(p != null ? p.getJoinedDate() : null)
                .isActive(u.isActive() && (p == null || p.isActive()))
                .roles(roles)
                .build();
    }

    public ShiftResponse toShiftResponse(Shift s) {
        List<ShiftAssignmentResponse> assignments = assignmentRepository.findByShift_Id(s.getId())
                .stream().map(this::toAssignmentResponse).toList();
        return ShiftResponse.builder()
                .id(s.getId()).shiftDate(s.getShiftDate())
                .startTime(s.getStartTime()).endTime(s.getEndTime())
                .roleRequired(s.getRoleRequired())
                .requiredStaffCount(s.getRequiredStaffCount())
                .status(s.getStatus().name()).assignments(assignments).build();
    }

    private ShiftAssignmentResponse toAssignmentResponse(ShiftAssignment a) {
        return ShiftAssignmentResponse.builder()
                .id(a.getId()).shiftId(a.getShift().getId())
                .staffId(a.getStaffId()).assignedRole(a.getAssignedRole())
                .status(a.getStatus().name()).build();
    }
}
