package com.ecogrid.ems.device.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DeviceStatusCache entity for fast real-time dashboard queries
 */
@Entity
@Table(name = "device_status_cache")
@EntityListeners(AuditingEntityListener.class)
public class DeviceStatusCache {

    @Id
    private Long deviceId; // Same as device ID, one-to-one relationship

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "device_id")
    private Device device;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status is required")
    private DeviceStatus status; // 'ONLINE', 'OFFLINE', 'FAULT', 'MAINTENANCE'

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "current_data", columnDefinition = "jsonb")
    private Map<String, Object> currentData; // Latest telemetry snapshot

    @Column(name = "alert_count", nullable = false)
    private Integer alertCount = 0; // Active alerts count

    @Column(name = "uptime_24h", precision = 5, scale = 2)
    private BigDecimal uptime24h; // 24-hour uptime percentage

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public DeviceStatusCache() {}

    public DeviceStatusCache(Device device, DeviceStatus status) {
        this.device = device;
        this.deviceId = device.getId();
        this.status = status;
    }

    // Getters and Setters
    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
        if (device != null) {
            this.deviceId = device.getId();
        }
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public Map<String, Object> getCurrentData() {
        return currentData;
    }

    public void setCurrentData(Map<String, Object> currentData) {
        this.currentData = currentData;
    }

    public Integer getAlertCount() {
        return alertCount;
    }

    public void setAlertCount(Integer alertCount) {
        this.alertCount = alertCount;
    }

    public BigDecimal getUptime24h() {
        return uptime24h;
    }

    public void setUptime24h(BigDecimal uptime24h) {
        this.uptime24h = uptime24h;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public enum DeviceStatus {
        ONLINE,
        OFFLINE,
        FAULT,
        MAINTENANCE
    }
}