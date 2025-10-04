package com.ecogrid.ems.device.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Site responses
 */
public record SiteResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("name")
        String name,

        @JsonProperty("description")
        String description,

        @JsonProperty("locationLat")
        BigDecimal locationLat,

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
        String contactPhone,

        @JsonProperty("deviceCount")
        Long deviceCount,

        @JsonProperty("createdAt")
        LocalDateTime createdAt,

        @JsonProperty("updatedAt")
        LocalDateTime updatedAt
) {
}