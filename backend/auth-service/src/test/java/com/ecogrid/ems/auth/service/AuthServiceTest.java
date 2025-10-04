package com.ecogrid.ems.auth.service;

import com.ecogrid.ems.auth.entity.User;
import com.ecogrid.ems.auth.repository.UserRepository;
import com.ecogrid.ems.auth.util.JwtUtil;
import com.ecogrid.ems.shared.dto.AuthRequest;
import com.ecogrid.ems.shared.dto.AuthResponse;
import com.ecogrid.ems.shared.dto.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;



    @InjectMocks
    private AuthService authService;

    private User testUser;
    private AuthRequest authRequest;
    private String token;
    private String refreshToken;
    private Long expirationTime;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(User.UserRole.OPERATOR);
        testUser.setAccountEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());

        authRequest = new AuthRequest("test@example.com", "password123");
        
        token = "test-jwt-token";
        refreshToken = "test-refresh-token";
        expirationTime = 3600L;
    }

        @Test
    @DisplayName("Should authenticate user successfully with valid credentials")
    void authenticate_ValidCredentials_ReturnsAuthResponse() {
        // Given
        when(userRepository.findByEmail(authRequest.email()))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(authRequest.password(), testUser.getPasswordHash()))
            .thenReturn(true);
        when(userRepository.save(any(User.class)))
            .thenReturn(testUser);
        when(jwtUtil.generateToken(testUser))
            .thenReturn(token);
        when(jwtUtil.generateRefreshToken(testUser))
            .thenReturn(refreshToken);
        when(jwtUtil.getExpirationTime())
            .thenReturn(expirationTime);

        // When
        AuthResponse response = authService.authenticate(authRequest);

        // Then
        assertNotNull(response);
        assertEquals(token, response.token());
        assertEquals(refreshToken, response.refreshToken());
        assertEquals(expirationTime, response.expiresIn());
        assertNotNull(response.user());
        assertEquals(testUser.getEmail(), response.user().email());

        verify(userRepository).findByEmail(authRequest.email());
        verify(passwordEncoder).matches(authRequest.password(), testUser.getPasswordHash());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(testUser);
        verify(jwtUtil).generateRefreshToken(testUser);
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when password is incorrect")
    void authenticate_InvalidCredentials_ThrowsBadCredentialsException() {
        // Given
        when(userRepository.findByEmail(authRequest.email()))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(authRequest.password(), testUser.getPasswordHash()))
            .thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> 
            authService.authenticate(authRequest));

        verify(userRepository).findByEmail(authRequest.email());
        verify(passwordEncoder).matches(authRequest.password(), testUser.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should register new user successfully")
    void registerUser_ValidData_ReturnsUserInfo() {
        // Given
        String email = "newuser@example.com";
        String password = "StrongPassword123";
        String firstName = "Jane";
        String lastName = "Smith";
        User.UserRole role = User.UserRole.VIEWER;

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserInfo userInfo = authService.registerUser(email, password, firstName, lastName, role);

        // Then
        assertNotNull(userInfo);
        assertEquals(testUser.getEmail(), userInfo.email());
        assertEquals(testUser.getFirstName(), userInfo.firstName());
        assertEquals(testUser.getLastName(), userInfo.lastName());

        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when registering existing user")
    void registerUser_ExistingEmail_ThrowsIllegalArgumentException() {
        // Given
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            authService.registerUser(email, "password", "John", "Doe", User.UserRole.VIEWER));

        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception for weak password")
    void registerUser_WeakPassword_ThrowsIllegalArgumentException() {
        // Given
        String email = "test@example.com";
        String weakPassword = "weak";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            authService.registerUser(email, weakPassword, "John", "Doe", User.UserRole.VIEWER));

        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should generate password reset token successfully")
    void generatePasswordResetToken_ValidEmail_ReturnsToken() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        String resetToken = authService.generatePasswordResetToken(email);

        // Then
        assertNotNull(resetToken);
        assertFalse(resetToken.isEmpty());

        verify(userRepository).findByEmail(email);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when generating reset token for non-existent user")
    void generatePasswordResetToken_InvalidEmail_ThrowsUsernameNotFoundException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> 
            authService.generatePasswordResetToken(email));

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reset password successfully with valid token")
    void resetPassword_ValidToken_ResetsPassword() {
        // Given
        String resetToken = UUID.randomUUID().toString();
        String newPassword = "NewStrongPassword123";
        String hashedNewPassword = "hashedNewPassword";

        testUser.setPasswordResetToken(resetToken);
        testUser.setPasswordResetTokenExpires(LocalDateTime.now().plusHours(1));

        when(userRepository.findByValidPasswordResetToken(eq(resetToken), any(LocalDateTime.class)))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        assertDoesNotThrow(() -> authService.resetPassword(resetToken, newPassword));

        // Then
        verify(userRepository).findByValidPasswordResetToken(eq(resetToken), any(LocalDateTime.class));
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception for invalid reset token")
    void resetPassword_InvalidToken_ThrowsIllegalArgumentException() {
        // Given
        String invalidToken = "invalid-token";
        String newPassword = "NewPassword123";

        when(userRepository.findByValidPasswordResetToken(eq(invalidToken), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            authService.resetPassword(invalidToken, newPassword));

        verify(userRepository).findByValidPasswordResetToken(eq(invalidToken), any(LocalDateTime.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should change password successfully with correct current password")
    void changePassword_CorrectCurrentPassword_ChangesPassword() {
        // Given
        String email = "test@example.com";
        String currentPassword = "currentPassword";
        String newPassword = "NewStrongPassword123";
        String hashedNewPassword = "hashedNewPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        assertDoesNotThrow(() -> authService.changePassword(email, currentPassword, newPassword));

        // Then
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(currentPassword, "hashedPassword");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception for incorrect current password")
    void changePassword_IncorrectCurrentPassword_ThrowsBadCredentialsException() {
        // Given
        String email = "test@example.com";
        String incorrectPassword = "incorrectPassword";
        String newPassword = "NewPassword123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(incorrectPassword, testUser.getPasswordHash())).thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> 
            authService.changePassword(email, incorrectPassword, newPassword));

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(incorrectPassword, testUser.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should load user by username successfully")
    void loadUserByUsername_ValidEmail_ReturnsUserDetails() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        var userDetails = authService.loadUserByUsername(email);

        // Then
        assertNotNull(userDetails);
        assertEquals(testUser.getUsername(), userDetails.getUsername());
        assertTrue(userDetails.isEnabled());

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw exception when loading non-existent user")
    void loadUserByUsername_InvalidEmail_ThrowsUsernameNotFoundException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> 
            authService.loadUserByUsername(email));

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should get user info successfully")
    void getUserInfo_ValidEmail_ReturnsUserInfo() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        Optional<UserInfo> userInfoOpt = authService.getUserInfo(email);

        // Then
        assertTrue(userInfoOpt.isPresent());
        UserInfo userInfo = userInfoOpt.get();
        assertEquals(testUser.getEmail(), userInfo.email());
        assertEquals(testUser.getFirstName(), userInfo.firstName());
        assertEquals(testUser.getLastName(), userInfo.lastName());

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void updateUserProfile_ValidData_ReturnsUpdatedUserInfo() {
        // Given
        String email = "test@example.com";
        String newFirstName = "UpdatedJohn";
        String newLastName = "UpdatedDoe";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserInfo updatedUserInfo = authService.updateUserProfile(email, newFirstName, newLastName);

        // Then
        assertNotNull(updatedUserInfo);
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(testUser);
    }
}