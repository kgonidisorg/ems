package com.ecogrid.ems.auth.controller;

import com.ecogrid.ems.auth.dto.*;
import com.ecogrid.ems.auth.entity.User;
import com.ecogrid.ems.auth.service.AuthService;
import com.ecogrid.ems.shared.dto.AuthRequest;
import com.ecogrid.ems.shared.dto.AuthResponse;
import com.ecogrid.ems.shared.dto.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController
 */
@WebMvcTest(AuthController.class)
@ExtendWith(MockitoExtension.class)
@Import(com.ecogrid.ems.auth.config.TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private AuthRequest validAuthRequest;
    private RegisterRequest validRegisterRequest;
    private ForgotPasswordRequest validForgotPasswordRequest;
    private ResetPasswordRequest validResetPasswordRequest;
    private ChangePasswordRequest validChangePasswordRequest;
    private AuthResponse mockAuthResponse;
    private UserInfo mockUserInfo;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Setup test data
        validAuthRequest = new AuthRequest("test@example.com", "password123");
        
        validRegisterRequest = new RegisterRequest(
                "newuser@example.com",
                "password123",
                "John",
                "Doe",
                "VIEWER"
        );

        validForgotPasswordRequest = new ForgotPasswordRequest("test@example.com");
        
        validResetPasswordRequest = new ResetPasswordRequest(
                "reset-token-123",
                "newpassword123"
        );

        validChangePasswordRequest = new ChangePasswordRequest(
                "oldpassword123",
                "newpassword123"
        );

        mockAuthResponse = new AuthResponse(
                "jwt-token-123",
                "refresh-token-123",
                3600L,
                new UserInfo(
                        1L,
                        "test@example.com",
                        "John",
                        "Doe",
                        "VIEWER",
                        "2023-01-01T10:00:00"
                )
        );

        mockUserInfo = new UserInfo(
                1L,
                "test@example.com",
                "John",
                "Doe",
                "VIEWER",
                "2023-01-01T10:00:00"
        );
    }

    // Login Tests
    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() throws Exception {
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("John"))
                .andExpect(jsonPath("$.user.lastName").value("Doe"));

        verify(authService, times(1)).authenticate(any(AuthRequest.class));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isInternalServerError());

        verify(authService, times(1)).authenticate(any(AuthRequest.class));
    }

    @Test
    void login_WithMissingEmail_ShouldReturnBadRequest() throws Exception {
        AuthRequest invalidRequest = new AuthRequest("", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).authenticate(any(AuthRequest.class));
    }

    @Test
    void login_WithInvalidEmailFormat_ShouldReturnBadRequest() throws Exception {
        AuthRequest invalidRequest = new AuthRequest("invalid-email", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).authenticate(any(AuthRequest.class));
    }

    // Registration Tests
    @Test
    void register_WithValidData_ShouldReturnCreatedUser() throws Exception {
        when(authService.registerUser(anyString(), anyString(), anyString(), anyString(), any(User.UserRole.class)))
                .thenReturn(mockUserInfo);

        mockMvc.perform(post("/api/v1/auth/register")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("VIEWER"));

        verify(authService, times(1)).registerUser(
                eq("newuser@example.com"),
                eq("password123"),
                eq("John"),
                eq("Doe"),
                eq(User.UserRole.VIEWER)
        );
    }

    @Test
    void register_WithDefaultRole_ShouldUseViewerRole() throws Exception {
        RegisterRequest requestWithoutRole = new RegisterRequest(
                "newuser@example.com",
                "password123",
                "John",
                "Doe",
                null
        );

        when(authService.registerUser(anyString(), anyString(), anyString(), anyString(), any(User.UserRole.class)))
                .thenReturn(mockUserInfo);

        mockMvc.perform(post("/api/v1/auth/register")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithoutRole)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).registerUser(
                eq("newuser@example.com"),
                eq("password123"),
                eq("John"),
                eq("Doe"),
                eq(User.UserRole.VIEWER)
        );
    }

    @Test
    void register_WithInvalidRole_ShouldReturnBadRequest() throws Exception {
        RegisterRequest invalidRoleRequest = new RegisterRequest(
                "newuser@example.com",
                "password123",
                "John",
                "Doe",
                "INVALID_ROLE"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRoleRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(anyString(), anyString(), anyString(), anyString(), any(User.UserRole.class));
    }

    @Test
    void register_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        RegisterRequest shortPasswordRequest = new RegisterRequest(
                "newuser@example.com",
                "123",
                "John",
                "Doe",
                "VIEWER"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortPasswordRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(anyString(), anyString(), anyString(), anyString(), any(User.UserRole.class));
    }

    // Forgot Password Tests
    @Test
    void forgotPassword_WithValidEmail_ShouldReturnResetToken() throws Exception {
        String resetToken = "reset-token-123";
        when(authService.generatePasswordResetToken(anyString())).thenReturn(resetToken);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validForgotPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset token generated"))
                .andExpect(jsonPath("$.token").value(resetToken));

        verify(authService, times(1)).generatePasswordResetToken("test@example.com");
    }

    @Test
    void forgotPassword_WithServiceException_ShouldReturnBadRequest() throws Exception {
        when(authService.generatePasswordResetToken(anyString()))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validForgotPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Failed to generate reset token"));

        verify(authService, times(1)).generatePasswordResetToken("test@example.com");
    }

    // Reset Password Tests
    @Test
    void resetPassword_WithValidToken_ShouldReturnSuccess() throws Exception {
        doNothing().when(authService).resetPassword(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validResetPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        verify(authService, times(1)).resetPassword("reset-token-123", "newpassword123");
    }

    @Test
    void resetPassword_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Invalid or expired token"))
                .when(authService).resetPassword(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validResetPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid or expired token"));

        verify(authService, times(1)).resetPassword("reset-token-123", "newpassword123");
    }

    // Change Password Tests
    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_WithValidData_ShouldReturnSuccess() throws Exception {
        doNothing().when(authService).changePassword(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/change-password")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validChangePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(authService, times(1)).changePassword("test@example.com", "oldpassword123", "newpassword123");
    }

    @Test
    void changePassword_WithoutAuthentication_ShouldSucceedWithAnonymousUser() throws Exception {
        doNothing().when(authService).changePassword(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/change-password")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validChangePasswordRequest)))
                .andExpect(status().isOk());

        verify(authService, times(1)).changePassword(eq("anonymousUser"), anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_WithWrongCurrentPassword_ShouldReturnBadRequest() throws Exception {
        doThrow(new RuntimeException("Current password is incorrect"))
                .when(authService).changePassword(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/change-password")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validChangePasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Current password is incorrect"));

        verify(authService, times(1)).changePassword("test@example.com", "oldpassword123", "newpassword123");
    }

    // Profile Tests
    @Test
    @WithMockUser(username = "test@example.com")
    void getProfile_WithAuthentication_ShouldReturnUserInfo() throws Exception {
        when(authService.getUserInfo("test@example.com")).thenReturn(Optional.of(mockUserInfo));

        mockMvc.perform(get("/api/v1/auth/profile")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("VIEWER"));

        verify(authService, times(1)).getUserInfo("test@example.com");
    }

    @Test
    void getProfile_WithoutAuthentication_ShouldReturnNotFound() throws Exception {
        when(authService.getUserInfo("anonymousUser")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/auth/profile")
                )
                .andExpect(status().isNotFound());

        verify(authService, times(1)).getUserInfo("anonymousUser");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getProfile_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        when(authService.getUserInfo("test@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/auth/profile")
                )
                .andExpect(status().isNotFound());

        verify(authService, times(1)).getUserInfo("test@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfile_WithValidData_ShouldReturnUpdatedUserInfo() throws Exception {
        Map<String, String> updateRequest = Map.of(
                "firstName", "Jane",
                "lastName", "Smith"
        );

        UserInfo updatedUserInfo = new UserInfo(
                1L,
                "test@example.com",
                "Jane",
                "Smith",
                "VIEWER",
                "2023-01-01T10:00:00"
        );

        when(authService.updateUserProfile("test@example.com", "Jane", "Smith"))
                .thenReturn(updatedUserInfo);

        mockMvc.perform(put("/api/v1/auth/profile")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(authService, times(1)).updateUserProfile("test@example.com", "Jane", "Smith");
    }

    @Test
    void updateProfile_WithoutAuthentication_ShouldSucceedWithAnonymousUser() throws Exception {
        Map<String, String> updateRequest = Map.of(
                "firstName", "Jane",
                "lastName", "Smith"
        );

        UserInfo updatedUserInfo = new UserInfo(
                1L,
                "anonymousUser",
                "Jane",
                "Smith",
                "VIEWER",
                "2023-01-01T10:00:00"
        );

        when(authService.updateUserProfile("anonymousUser", "Jane", "Smith"))
                .thenReturn(updatedUserInfo);

        mockMvc.perform(put("/api/v1/auth/profile")
                
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        verify(authService, times(1)).updateUserProfile("anonymousUser", "Jane", "Smith");
    }

    // Health Check Tests
    @Test
    void health_ShouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/api/v1/auth/health")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("auth-service"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}