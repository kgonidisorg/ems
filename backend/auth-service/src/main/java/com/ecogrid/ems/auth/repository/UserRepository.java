package com.ecogrid.ems.auth.repository;

import com.ecogrid.ems.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find user by password reset token
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Find user by password reset token that hasn't expired
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetTokenExpires > :now")
    Optional<User> findByValidPasswordResetToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Update user's last login timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("lastLogin") LocalDateTime lastLogin);

    /**
     * Clear password reset token after use
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = NULL, u.passwordResetTokenExpires = NULL WHERE u.id = :userId")
    void clearPasswordResetToken(@Param("userId") Long userId);

    /**
     * Count total number of users
     */
    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();

    /**
     * Count active users (enabled accounts)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountEnabled = true")
    long countActiveUsers();
}