package com.ecogrid.ems.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for Device creation and updates
 */
public record DeviceRequest(
        @NotBlank(message = "Serial number is required")
        @Size(max = 100, message = "Serial number must not exceed 100 characters")
        @JsonProperty("serialNumber")
        String serialNumber,

        @NotBlank(message = "Device name is required")
        @Size(max = 100, message = "Device name must not exceed 100 characters")
        @JsonProperty("name")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        @JsonProperty("description")
        String description,

        @NotBlank(message = "Device type is required")
        @JsonProperty("deviceType")
        String deviceType,

        @NotBlank(message = "Model is required")
        @Size(max = 100, message = "Model must not exceed 100 characters")
        @JsonProperty("model")
        String model,

        @NotBlank(message = "Manufacturer is required")
        @Size(max = 100, message = "Manufacturer must not exceed 100 characters")
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

        @NotNull(message = "Site ID is required")
        @JsonProperty("siteId")
        Long siteId,

        @JsonProperty("configuration")
        Map<String, String> configuration,

        @JsonProperty("metadata")
        Map<String, String> metadata
) {
}