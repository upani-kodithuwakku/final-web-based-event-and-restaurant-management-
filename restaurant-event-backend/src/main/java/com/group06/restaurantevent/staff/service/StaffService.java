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

    // ---- Staff user creation (P2) ----

    @Transactional
    public StaffProfileResponse createStaffUser(CreateStaffUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already registered: " + req.getEmail());
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : req.getRoles()) {
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

    @Transactional(readOnly = true)
    public List<StaffProfileResponse> listStaff() {
        return userRepository.findAllExcludingRole("CUSTOMER")
                .stream().map(u -> {
                    StaffProfile p = profileRepository.findByUserId(u.getId()).orElse(null);
                    return toUserResponse(u, p);
                }).toList();
    }

    @Transactional(readOnly = true)
    public StaffProfileResponse getStaff(Long id) {
        StaffProfile p = profileRepository.findById(id).orElse(null);
        if (p != null) {
            User u = userRepository.findById(p.getUserId()).orElse(null);
            return toProfileResponse(p, u);
        }
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));
        return toUserResponse(u, null);
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
        Set<Role> roles = roleNames.stream()
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
        if (newPassword == null || newPassword.length() < 8)
            throw new BadRequestException("Password must be at least 8 characters");
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
        findProfile(staffId);

        if (!assignmentRepository.findOverlapping(staffId, shift.getShiftDate(),
                shift.getStartTime(), shift.getEndTime()).isEmpty())
            throw new ConflictException("Staff member already has an overlapping shift on this date");

        if (assignmentRepository.findByShift_IdAndStaffId(shiftId, staffId).isPresent())
            throw new ConflictException("Staff already assigned to this shift");

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
        assignmentRepository.delete(assignment);
    }

    // ---- Helpers ----

    private StaffProfile findProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found: " + id));
    }

    private Shift findShift(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + id));
    }

    private String generateEmpCode() {
        String suffix = String.format("%04d", new Random().nextInt(9999));
        String code = "EMP-" + suffix;
        return profileRepository.existsByEmployeeCode(code) ? generateEmpCode() : code;
    }

    private EmploymentStatus parseStatus(String s) {
        try { return EmploymentStatus.valueOf(s.toUpperCase()); }
        catch (Exception e) { return EmploymentStatus.FULL_TIME; }
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
                .isActive(u.isActive())
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
