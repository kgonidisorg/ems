package com.ecogrid.ems.gateway.util;

import com.ecogrid.ems.gateway.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtUtil
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private JwtProperties jwtProperties;

    private JwtUtil jwtUtil;
    private String secretKey = "mySecretKeyForTestingPurposesItShouldBeLongEnoughForHS256";
    private SecretKey key;

    @BeforeEach
    void setUp() {
        when(jwtProperties.getSecret()).thenReturn(secretKey);
        jwtUtil = new JwtUtil(jwtProperties);
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        // Arrange
        String validToken = createValidToken("test@example.com", 1L, "ADMIN");

        // Act
        boolean result = jwtUtil.validateToken(validToken);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateToken_ExpiredToken_ShouldReturnFalse() {
        // Arrange
        String expiredToken = createExpiredToken("test@example.com", 1L, "ADMIN");

        // Act
        boolean result = jwtUtil.validateToken(expiredToken);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateToken_InvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean result = jwtUtil.validateToken(invalidToken);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateToken_NullToken_ShouldReturnFalse() {
        // Act
        boolean result = jwtUtil.validateToken(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateToken_EmptyToken_ShouldReturnFalse() {
        // Act
        boolean result = jwtUtil.validateToken("");

        // Assert
        assertFalse(result);
    }

    @Test
    void extractUsername_ValidToken_ShouldReturnUsername() {
        // Arrange
        String username = "test@example.com";
        String validToken = createValidToken(username, 1L, "ADMIN");

        // Act
        String result = jwtUtil.extractUsername(validToken);

        // Assert
        assertEquals(username, result);
    }

    @Test
    void extractUserId_ValidToken_ShouldReturnUserId() {
        // Arrange
        Long userId = 123L;
        String validToken = createValidToken("test@example.com", userId, "ADMIN");

        // Act
        Long result = jwtUtil.extractUserId(validToken);

        // Assert
        assertEquals(userId, result);
    }

    @Test
    void extractRole_ValidToken_ShouldReturnRole() {
        // Arrange
        String role = "ADMIN";
        String validToken = createValidToken("test@example.com", 1L, role);

        // Act
        String result = jwtUtil.extractRole(validToken);

        // Assert
        assertEquals(role, result);
    }

    @Test
    void extractUsername_InvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractUsername(invalidToken));
    }

    @Test
    void extractUserId_InvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractUserId(invalidToken));
    }

    @Test
    void extractRole_InvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractRole(invalidToken));
    }

    @Test
    void extractUsername_ExpiredToken_ShouldThrowException() {
        // Arrange
        String username = "test@example.com";
        String expiredToken = createExpiredToken(username, 1L, "ADMIN");

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractUsername(expiredToken));
    }

    @Test
    void extractUserId_ExpiredToken_ShouldThrowException() {
        // Arrange
        Long userId = 123L;
        String expiredToken = createExpiredToken("test@example.com", userId, "ADMIN");

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractUserId(expiredToken));
    }

    @Test
    void extractRole_ExpiredToken_ShouldThrowException() {
        // Arrange
        String role = "USER";
        String expiredToken = createExpiredToken("test@example.com", 1L, role);

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractRole(expiredToken));
    }

    private String createValidToken(String username, Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(key)
                .compact();
    }

    private String createExpiredToken(String username, Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)) // 1 day ago
                .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 hour ago
                .signWith(key)
                .compact();
    }
}