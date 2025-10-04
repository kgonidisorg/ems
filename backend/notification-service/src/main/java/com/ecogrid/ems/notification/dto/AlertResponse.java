package com.ecogrid.ems.notification.dto;

import com.ecogrid.ems.notification.entity.Alert;
import java.time.LocalDateTime;

public class AlertResponse {
    
    private Long id;
    private Long deviceId;
    private Long siteId;
    private String type;
    private Alert.AlertSeverity severity;
    private String message;
    private String description;
    private boolean acknowledged;
    private Long acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String metadata;
    
    // Constructors
    public AlertResponse() {}
    
    public AlertResponse(Alert alert) {
        this.id = alert.getId();
        this.deviceId = alert.getDeviceId();
        this.siteId = alert.getSiteId();
        this.type = alert.getType();
        this.severity = alert.getSeverity();
        this.message = alert.getMessage();
        this.description = alert.getDescription();
        this.acknowledged = alert.isAcknowledged();
        this.acknowledgedBy = alert.getAcknowledgedBy();
        this.acknowledgedAt = alert.getAcknowledgedAt();
        this.createdAt = alert.getCreatedAt();
        this.resolvedAt = alert.getResolvedAt();
        this.metadata = alert.getMetadata();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public boolean isAcknowledged() {
        return acknowledged;
    }
    
    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
    
    public Long getAcknowledgedBy() {
        return acknowledgedBy;
    }
    
    public void setAcknowledgedBy(Long acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }
    
    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }
    
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}