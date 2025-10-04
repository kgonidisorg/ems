package com.ecogrid.ems.device.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Event published when device status changes
 */
public record DeviceStatusEvent(
        @JsonProperty("deviceId")
        Long deviceId,

        @JsonProperty("serialNumber")
        String serialNumber,

        @JsonProperty("siteId")
        Long siteId,

        @JsonProperty("previousStatus")
        String previousStatus,

        @JsonProperty("newStatus")
        String newStatus,

        @JsonProperty("timestamp")
        LocalDateTime timestamp,

        @JsonProperty("eventType")
        String eventType,

        @JsonProperty("reason")
        String reason
) {
    public static DeviceStatusEvent create(Long deviceId, String serialNumber, Long siteId, 
                                         String previousStatus, String newStatus, String reason) {
        return new DeviceStatusEvent(
                deviceId,
                serialNumber,
                siteId,
                previousStatus,
                newStatus,
                LocalDateTime.now(),
                "DEVICE_STATUS_CHANGE",
                reason
        );
    }
}