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
 * DeviceType entity representing device type specifications and telemetry schema
 */
@Entity
@Table(name = "device_types", indexes = {
    @Index(name = "idx_device_type_name", columnList = "name"),
    @Index(name = "idx_device_type_category", columnList = "category")
})
@EntityListeners(AuditingEntityListener.class)
public class DeviceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotNull(message = "Device type name is required")
    private String name; // 'BMS', 'SOLAR_ARRAY', 'EV_CHARGER'

    @Column(nullable = false)
    @NotNull(message = "Category is required")
    private String category; // 'STORAGE', 'GENERATION', 'CHARGING'

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "telemetry_schema", columnDefinition = "jsonb")
    private Map<String, Object> telemetrySchema; // Device-specific telemetry structure

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "alert_thresholds", columnDefinition = "jsonb")
    private Map<String, Object> alertThresholds; // Alert configuration

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specifications", columnDefinition = "jsonb")
    private Map<String, Object> specifications; // Device specifications

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public DeviceType() {}

    public DeviceType(String name, String category) {
        this.name = name;
        this.category = category;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Map<String, Object> getTelemetrySchema() {
        return telemetrySchema;
    }

    public void setTelemetrySchema(Map<String, Object> telemetrySchema) {
        this.telemetrySchema = telemetrySchema;
    }

    public Map<String, Object> getAlertThresholds() {
        return alertThresholds;
    }

    public void setAlertThresholds(Map<String, Object> alertThresholds) {
        this.alertThresholds = alertThresholds;
    }

    public Map<String, Object> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Map<String, Object> specifications) {
        this.specifications = specifications;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}