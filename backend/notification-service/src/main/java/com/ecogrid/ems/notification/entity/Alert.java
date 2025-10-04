package com.ecogrid.ems.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "device_id")
    private Long deviceId;
    
    @NotNull
    @Column(name = "site_id")
    private Long siteId;
    
    @NotBlank
    @Column(name = "type")
    private String type;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private AlertSeverity severity;
    
    @NotBlank
    @Column(name = "message", length = 1000)
    private String message;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "acknowledged", nullable = false)
    private boolean acknowledged = false;
    
    @Column(name = "acknowledged_by")
    private Long acknowledgedBy;
    
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
    
    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "metadata", length = 4000)
    private String metadata; // JSON string for additional alert data
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Alert() {}
    
    public Alert(Long deviceId, Long siteId, String type, AlertSeverity severity, String message) {
        this.deviceId = deviceId;
        this.siteId = siteId;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.createdAt = LocalDateTime.now();
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
    
    public AlertSeverity getSeverity() {
        return severity;
    }
    
    public void setSeverity(AlertSeverity severity) {
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
    
    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}