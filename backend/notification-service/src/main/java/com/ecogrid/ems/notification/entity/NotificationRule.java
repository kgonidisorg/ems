package com.ecogrid.ems.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_rules")
public class NotificationRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(name = "name")
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @NotNull
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "site_id")
    private Long siteId; // null means all sites
    
    @Column(name = "device_id")
    private Long deviceId; // null means all devices
    
    @NotBlank
    @Column(name = "alert_type")
    private String alertType; // Can be wildcard like "BATTERY_*" or specific "BATTERY_LOW"
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "min_severity")
    private Alert.AlertSeverity minSeverity;
    
    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;
    
    @Column(name = "websocket_enabled", nullable = false)
    private boolean websocketEnabled = true;
    
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
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
    public NotificationRule() {}
    
    public NotificationRule(String name, Long userId, String alertType, Alert.AlertSeverity minSeverity) {
        this.name = name;
        this.userId = userId;
        this.alertType = alertType;
        this.minSeverity = minSeverity;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getSiteId() {
        return siteId;
    }
    
    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }
    
    public Long getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getAlertType() {
        return alertType;
    }
    
    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }
    
    public Alert.AlertSeverity getMinSeverity() {
        return minSeverity;
    }
    
    public void setMinSeverity(Alert.AlertSeverity minSeverity) {
        this.minSeverity = minSeverity;
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
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
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
}