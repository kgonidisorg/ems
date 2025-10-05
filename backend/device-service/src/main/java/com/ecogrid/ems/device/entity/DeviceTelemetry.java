package com.ecogrid.ems.device.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DeviceTelemetry entity for storing time-series device telemetry data
 */
@Entity
@Table(name = "device_telemetry", indexes = {
    @Index(name = "idx_telemetry_device_timestamp", columnList = "device_id, timestamp DESC"),
    @Index(name = "idx_telemetry_timestamp", columnList = "timestamp DESC")
})
@EntityListeners(AuditingEntityListener.class)
public class DeviceTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    @NotNull(message = "Device is required")
    private Device device;

    @Column(nullable = false)
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> data; // Type-specific telemetry data

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quality_indicators", columnDefinition = "jsonb")
    private Map<String, Object> qualityIndicators; // Signal quality, connection status

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public DeviceTelemetry() {}

    public DeviceTelemetry(Device device, LocalDateTime timestamp, Map<String, Object> data) {
        this.device = device;
        this.timestamp = timestamp;
        this.data = data;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getQualityIndicators() {
        return qualityIndicators;
    }

    public void setQualityIndicators(Map<String, Object> qualityIndicators) {
        this.qualityIndicators = qualityIndicators;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}