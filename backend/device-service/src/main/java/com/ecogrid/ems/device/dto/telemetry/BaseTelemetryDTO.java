package com.ecogrid.ems.device.dto.telemetry;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base DTO for device telemetry data
 */
public abstract class BaseTelemetryDTO {

    @NotNull
    private Long deviceId;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private Map<String, Object> qualityIndicators;

    // Constructors
    public BaseTelemetryDTO() {}

    public BaseTelemetryDTO(Long deviceId, LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getQualityIndicators() {
        return qualityIndicators;
    }

    public void setQualityIndicators(Map<String, Object> qualityIndicators) {
        this.qualityIndicators = qualityIndicators;
    }
}