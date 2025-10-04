package com.ecogrid.ems.auth.service;

import com.ecogrid.ems.auth.entity.User;
import com.ecogrid.ems.auth.repository.UserRepository;
import com.ecogrid.ems.auth.util.JwtUtil;
import com.ecogrid.ems.shared.dto.AuthRequest;
import com.ecogrid.ems.shared.dto.AuthResponse;
import com.ecogrid.ems.shared.dto.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * Authentication service for user registration, login, and token management
 */
@Service
@Transactional
public class AuthService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder, 
                      JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticate user and generate JWT tokens
     */
    public AuthResponse authenticate(AuthRequest authRequest) {
        try {
            // Get user details
            User user = userRepository.findByEmail(authRequest.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + authRequest.email()));

            // Check if account is enabled
            if (!user.isAccountEnabled()) {
                throw new BadCredentialsException("Account is disabled");
            }

            // Verify password
            if (!passwordEncoder.matches(authRequest.password(), user.getPasswordHash())) {
                throw new BadCredentialsException("Invalid credentials");
            }

            // Update last login timestamp
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Generate JWT tokens
            String token = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            logger.info("User authenticated successfully: {}", user.getEmail());

            return new AuthResponse(
                token,
                refreshToken,
                jwtUtil.getExpirationTime(),
                mapToUserInfo(user)
            );

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for user: {}", authRequest.email());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Register a new user
     */
    public UserInfo registerUser(String email, String password, String firstName, String lastName, User.UserRole role) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }

        // Validate password strength
        validatePassword(password);

        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role != null ? role : User.UserRole.VIEWER);
        user.setAccountEnabled(true);

        // Save user
        User savedUser = userRepository.save(user);
        logger.info("New user registered: {}", savedUser.getEmail());

        return mapToUserInfo(savedUser);
    }

    /**
     * Generate password reset token
     */
    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpires(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour

        userRepository.save(user);
        logger.info("Password reset token generated for user: {}", email);

        return resetToken;
    }

    /**
     * Reset password using reset token
     */
    public void resetPassword(String resetToken, String newPassword) {
        User user = userRepository.findByValidPasswordResetToken(resetToken, LocalDateTime.now())
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        // Validate new password
        validatePassword(newPassword);

        // Update password and clear reset token
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpires(null);

        userRepository.save(user);
        logger.info("Password reset successfully for user: {}", user.getEmail());
    }

    /**
     * Change user password
     */
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Validate new password
        validatePassword(newPassword);

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password changed successfully for user: {}", email);
    }

    /**
     * Get user information by email
     */
    public Optional<UserInfo> getUserInfo(String email) {
        return userRepository.findByEmail(email)
            .map(this::mapToUserInfo);
    }

    /**
     * Update user profile
     */
    public UserInfo updateUserProfile(String email, String firstName, String lastName) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        user.setFirstName(firstName);
        user.setLastName(lastName);

        User updatedUser = userRepository.save(user);
        logger.info("Profile updated successfully for user: {}", email);

        return mapToUserInfo(updatedUser);
    }

    /**
     * Load user by username (email) for Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    /**
     * Validate password strength
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }

    /**
     * Map User entity to UserInfo DTO
     */
    private UserInfo mapToUserInfo(User user) {
        return new UserInfo(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name(),
            user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_FORMATTER) : null
        );
    }
}