package com.ecogrid.ems.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for Report creation request
 */
public record ReportRequest(
        @JsonProperty("name")
        @NotBlank(message = "Report name is required")
        String name,

        @JsonProperty("description")
        String description,

        @JsonProperty("reportType")
        @NotNull(message = "Report type is required")
        String reportType,

        @JsonProperty("startDate")
        LocalDateTime startDate,

        @JsonProperty("endDate")
        LocalDateTime endDate,

        @JsonProperty("siteId")
        Long siteId,

        @JsonProperty("format")
        String format, // PDF, CSV, JSON

        @JsonProperty("parameters")
        Map<String, String> parameters,

        @JsonProperty("scheduledFrequency")
        String scheduledFrequency // ONCE, DAILY, WEEKLY, MONTHLY

) {}