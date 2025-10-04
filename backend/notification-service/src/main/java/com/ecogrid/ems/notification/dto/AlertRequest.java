package com.ecogrid.ems.notification.dto;

import com.ecogrid.ems.notification.entity.Alert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AlertRequest {
    
    @NotNull
    private Long deviceId;
    
    @NotNull
    private Long siteId;
    
    @NotBlank
    private String type;
    
    @NotNull
    private Alert.AlertSeverity severity;
    
    @NotBlank
    private String message;
    
    private String description;
    
    private String metadata;
    
    // Constructors
    public AlertRequest() {}
    
    public AlertRequest(Long deviceId, Long siteId, String type, Alert.AlertSeverity severity, String message) {
        this.deviceId = deviceId;
        this.siteId = siteId;
        this.type = type;
        this.severity = severity;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }
    
    public Long getSiteId() {
        return siteId;
    }
    
    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Alert.AlertSeverity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Alert.AlertSeverity severity) {
        this.severity = severity;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}