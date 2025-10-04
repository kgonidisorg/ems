package com.ecogrid.ems.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for Device responses
 */
public record DeviceResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("serialNumber")
        String serialNumber,

        @JsonProperty("name")
        String name,

        @JsonProperty("description")
        String description,

        @JsonProperty("deviceType")
        String deviceType,

        @JsonProperty("model")
        String model,

        @JsonProperty("manufacturer")
        String manufacturer,

        @JsonProperty("firmwareVersion")
        String firmwareVersion,

        @JsonProperty("status")
        String status,

        @JsonProperty("ratedPowerKw")
        BigDecimal ratedPowerKw,

        @JsonProperty("mqttTopic")
        String mqttTopic,

        @JsonProperty("ipAddress")
        String ipAddress,

        @JsonProperty("macAddress")
        String macAddress,

        @JsonProperty("installationDate")
        LocalDateTime installationDate,

        @JsonProperty("lastCommunication")
        LocalDateTime lastCommunication,

        @JsonProperty("lastMaintenance")
        LocalDateTime lastMaintenance,

        @JsonProperty("siteId")
        Long siteId,

        @JsonProperty("siteName")
        String siteName,

        @JsonProperty("configuration")
        Map<String, String> configuration,

        @JsonProperty("metadata")
        Map<String, String> metadata,

        @JsonProperty("createdAt")
        LocalDateTime createdAt,

        @JsonProperty("updatedAt")
        LocalDateTime updatedAt
) {
}