package com.ecogrid.ems.notification.dto;

import com.ecogrid.ems.notification.entity.Alert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationRuleRequest {
    
    @NotBlank
    private String name;
    
    private String description;
    
    @NotNull
    private Long userId;
    
    private Long siteId; // null means all sites
    private Long deviceId; // null means all devices
    
    @NotBlank
    private String alertType;
    
    @NotNull
    private Alert.AlertSeverity minSeverity;
    
    private boolean emailEnabled = true;
    private boolean websocketEnabled = true;
    private boolean active = true;
    
    // Constructors
    public NotificationRuleRequest() {}
    
    public NotificationRuleRequest(String name, Long userId, String alertType, Alert.AlertSeverity minSeverity) {
        this.name = name;
        this.userId = userId;
        this.alertType = alertType;
        this.minSeverity = minSeverity;
    }
    
    // Getters and Setters
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
}