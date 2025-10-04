package com.ecogrid.ems.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Site creation and updates
 */
public record SiteRequest(
        @NotBlank(message = "Site name is required")
        @Size(max = 100, message = "Site name must not exceed 100 characters")
        @JsonProperty("name")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        @JsonProperty("description")
        String description,

        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        @JsonProperty("locationLat")
        BigDecimal locationLat,

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        @JsonProperty("locationLng")
        BigDecimal locationLng,

        @JsonProperty("capacityMw")
        BigDecimal capacityMw,

        @JsonProperty("status")
        String status,

        @JsonProperty("timezone")
        String timezone,

        @JsonProperty("address")
        String address,

        @JsonProperty("contactPerson")
        String contactPerson,

        @JsonProperty("contactEmail")
        String contactEmail,

        @JsonProperty("contactPhone")
        String contactPhone
) {
}