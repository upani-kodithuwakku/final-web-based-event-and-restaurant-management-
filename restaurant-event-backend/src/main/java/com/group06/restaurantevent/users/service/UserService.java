package com.group06.restaurantevent.users.service;

import com.group06.restaurantevent.common.audit.AuditLog;
import com.group06.restaurantevent.common.audit.AuditLogRepository;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.common.exception.ResourceNotFoundException;
import com.group06.restaurantevent.users.dto.request.AdminResetPasswordRequest;
import com.group06.restaurantevent.users.dto.request.ChangePasswordRequest;
import com.group06.restaurantevent.users.dto.request.UpdateProfileRequest;
import com.group06.restaurantevent.users.dto.response.AdminUserResponse;
import com.group06.restaurantevent.users.dto.response.UserProfileResponse;
import com.group06.restaurantevent.users.entity.Role;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditLogRepository;

    // ── Own profile ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        return toProfile(findActiveUser(email));
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest req) {
        User user = findActiveUser(email);
        user.setFullName(req.getFullName().trim());
        if (req.getPhone() != null) user.setPhone(req.getPhone().isBlank() ? null : req.getPhone().trim());
        return toProfile(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = findActiveUser(email);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash()))
            throw new BadRequestException("Current password is incorrect");
        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash()))
            throw new BadRequestException("New password must be different from the current password");
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    // ── Admin user management ─────────────────────────────────

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(this::toAdmin).toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(Long id) {
        return toAdmin(findUser(id));
    }

    @Transactional
    public AdminUserResponse setActive(Long id, boolean active, String adminEmail) {
        User user = findUser(id);
        if (!active && user.getEmail().equalsIgnoreCase(adminEmail))
            throw new BadRequestException("You cannot suspend your own account");
        boolean before = user.isActive();
        user.setActive(active);
        AdminUserResponse response = toAdmin(userRepository.save(user));
        audit(adminEmail, active ? "USER_ACTIVATED" : "USER_SUSPENDED", user.getId(),
                String.valueOf(before), String.valueOf(active));
        return response;
    }

    @Transactional
    public void resetPassword(Long id, AdminResetPasswordRequest req, String adminEmail) {
        User user = findUser(id);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);
        audit(adminEmail, "USER_PASSWORD_RESET", user.getId(), null, null);
    }

    /** Users are deactivated, never hard-deleted, because orders and bookings reference them. */
    @Transactional
    public void deactivate(Long id, String adminEmail) {
        setActive(id, false, adminEmail);
    }

    @Transactional
    public AdminUserResponse updateUser(Long id, UpdateProfileRequest req, String adminEmail) {
        User user=findUser(id);user.setFullName(req.getFullName().trim());user.setPhone(req.getPhone());
        audit(adminEmail,"USER_PROFILE_UPDATED",id,null,null);
        return toAdmin(userRepository.save(user));
    }

    // ── Helpers ───────────────────────────────────────────────

    private void audit(String adminEmail, String action, Long userId, String oldValue, String newValue) {
        Long adminId = userRepository.findByEmail(adminEmail).map(User::getId).orElse(null);
        auditLogRepository.save(AuditLog.builder().userId(adminId).action(action)
                .entityName("User").entityId(userId).oldValue(oldValue).newValue(newValue).build());
    }

    private User findActiveUser(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private List<String> roleNames(User user) {
        return user.getRoles().stream().map(Role::getName).sorted().toList();
    }

    private UserProfileResponse toProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone() != null ? user.getPhone() : "")
                .roles(roleNames(user))
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AdminUserResponse toAdmin(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName() != null ? user.getFullName() : "")
                .email(user.getEmail())
                .phone(user.getPhone() != null ? user.getPhone() : "")
                .roles(roleNames(user))
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
