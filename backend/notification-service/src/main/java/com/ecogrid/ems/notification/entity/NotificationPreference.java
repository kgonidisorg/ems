package com.ecogrid.ems.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "user_id", unique = true)
    private Long userId;
    
    @Email
    @Column(name = "email")
    private String email;
    
    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;
    
    @Column(name = "websocket_enabled", nullable = false)
    private boolean websocketEnabled = true;
    
    @Column(name = "digest_enabled", nullable = false)
    private boolean digestEnabled = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "digest_frequency")
    private DigestFrequency digestFrequency = DigestFrequency.DAILY;
    
    @Column(name = "quiet_hours_start")
    private Integer quietHoursStart; // Hour in 24h format (0-23)
    
    @Column(name = "quiet_hours_end")
    private Integer quietHoursEnd; // Hour in 24h format (0-23)
    
    @Column(name = "timezone")
    private String timezone = "UTC";
    
    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public NotificationPreference() {}
    
    public NotificationPreference(Long userId, String email) {
        this.userId = userId;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isEmailEnabled() {
        return emailEnabled;
    }
    
    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }
    
    public boolean isWebsocketEnabled() {
        return websocketEnabled;
    }
    
    public void setWebsocketEnabled(boolean websocketEnabled) {
        this.websocketEnabled = websocketEnabled;
    }
    
    public boolean isDigestEnabled() {
        return digestEnabled;
    }
    
    public void setDigestEnabled(boolean digestEnabled) {
        this.digestEnabled = digestEnabled;
    }
    
    public DigestFrequency getDigestFrequency() {
        return digestFrequency;
    }
    
    public void setDigestFrequency(DigestFrequency digestFrequency) {
        this.digestFrequency = digestFrequency;
    }
    
    public Integer getQuietHoursStart() {
        return quietHoursStart;
    }
    
    public void setQuietHoursStart(Integer quietHoursStart) {
        this.quietHoursStart = quietHoursStart;
    }
    
    public Integer getQuietHoursEnd() {
        return quietHoursEnd;
    }
    
    public void setQuietHoursEnd(Integer quietHoursEnd) {
        this.quietHoursEnd = quietHoursEnd;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public enum DigestFrequency {
        HOURLY, DAILY, WEEKLY
    }
}