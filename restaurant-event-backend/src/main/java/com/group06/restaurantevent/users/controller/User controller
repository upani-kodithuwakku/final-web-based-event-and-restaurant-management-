package com.group06.restaurantevent.users.controller;

import com.group06.restaurantevent.common.response.ApiResponse;
import com.group06.restaurantevent.users.dto.request.ChangePasswordRequest;
import com.group06.restaurantevent.users.dto.request.UpdateProfileRequest;
import com.group06.restaurantevent.users.dto.response.UserProfileResponse;
import com.group06.restaurantevent.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(principal.getUsername())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my name and phone")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@AuthenticationPrincipal UserDetails principal,
                                                                         @Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                userService.updateProfile(principal.getUsername(), req)));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change my password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal UserDetails principal,
                                                            @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(principal.getUsername(), req);
        return ResponseEntity.ok(ApiResponse.success("Password changed", null));
    }
}
