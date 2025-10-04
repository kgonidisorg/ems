package com.ecogrid.ems.device.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event published when device telemetry data is received
 */
public record DeviceTelemetryEvent(
        @JsonProperty("deviceId")
        Long deviceId,

        @JsonProperty("serialNumber")
        String serialNumber,

        @JsonProperty("siteId")
        Long siteId,

        @JsonProperty("deviceType")
        String deviceType,

        @JsonProperty("telemetryData")
        Map<String, Object> telemetryData,

        @JsonProperty("timestamp")
        LocalDateTime timestamp,

        @JsonProperty("eventType")
        String eventType
) {
    public static DeviceTelemetryEvent create(Long deviceId, String serialNumber, Long siteId, 
                                            String deviceType, Map<String, Object> telemetryData) {
        return new DeviceTelemetryEvent(
                deviceId,
                serialNumber,
                siteId,
                deviceType,
                telemetryData,
                LocalDateTime.now(),
                "DEVICE_TELEMETRY"
        );
    }
}