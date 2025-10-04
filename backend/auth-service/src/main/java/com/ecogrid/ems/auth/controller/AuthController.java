package com.ecogrid.ems.auth.controller;

import com.ecogrid.ems.auth.dto.*;
import com.ecogrid.ems.auth.entity.User;
import com.ecogrid.ems.auth.service.AuthService;
import com.ecogrid.ems.shared.dto.AuthRequest;
import com.ecogrid.ems.shared.dto.AuthResponse;
import com.ecogrid.ems.shared.dto.UserInfo;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST controller for authentication endpoints
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.authenticate(authRequest);
        logger.info("User logged in successfully: {}", authRequest.email());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * User registration endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<UserInfo> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Parse role or default to VIEWER
        User.UserRole role = User.UserRole.VIEWER;
        if (registerRequest.role() != null && !registerRequest.role().isEmpty()) {
            try {
                role = User.UserRole.valueOf(registerRequest.role().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + registerRequest.role());
            }
        }

        UserInfo userInfo = authService.registerUser(
            registerRequest.email(),
            registerRequest.password(),
            registerRequest.firstName(),
            registerRequest.lastName(),
            role
        );

        logger.info("User registered successfully: {}", registerRequest.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(userInfo);
    }

    /**
     * Forgot password endpoint
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String resetToken = authService.generatePasswordResetToken(request.email());
            logger.info("Password reset token generated for user: {}", request.email());
            
            // In a real application, you would send this token via email
            // For now, we'll return it in the response (NOT RECOMMENDED FOR PRODUCTION)
            return ResponseEntity.ok(Map.of(
                "message", "Password reset token generated",
                "token", resetToken // Remove this in production
            ));
        } catch (Exception e) {
            logger.error("Failed to generate password reset token for user: {}", request.email(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to generate reset token"));
        }
    }

    /**
     * Reset password endpoint
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.token(), request.newPassword());
            logger.info("Password reset successfully");
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Password reset failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Password reset failed"));
        }
    }

    /**
     * Change password endpoint (authenticated users only)
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            authService.changePassword(email, request.currentPassword(), request.newPassword());
            logger.info("Password changed successfully for user: {}", email);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));

        } catch (Exception e) {
            logger.error("Password change failed", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Optional<UserInfo> userInfo = authService.getUserInfo(email);
            if (userInfo.isPresent()) {
                return ResponseEntity.ok(userInfo.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to get user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get user profile"));
        }
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updateRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            String firstName = updateRequest.get("firstName");
            String lastName = updateRequest.get("lastName");

            UserInfo userInfo = authService.updateUserProfile(email, firstName, lastName);
            logger.info("Profile updated successfully for user: {}", email);
            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            logger.error("Profile update failed", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "auth-service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}