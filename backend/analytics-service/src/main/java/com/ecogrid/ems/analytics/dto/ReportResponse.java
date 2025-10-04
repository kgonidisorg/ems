package com.ecogrid.ems.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for Report response
 */
public record ReportResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("name")
        String name,

        @JsonProperty("description")
        String description,

        @JsonProperty("reportType")
        String reportType,

        @JsonProperty("status")
        String status, // PENDING, GENERATING, COMPLETED, FAILED

        @JsonProperty("format")
        String format,

        @JsonProperty("filePath")
        String filePath,

        @JsonProperty("fileSize")
        Long fileSize,

        @JsonProperty("startDate")
        LocalDateTime startDate,

        @JsonProperty("endDate")
        LocalDateTime endDate,

        @JsonProperty("siteId")
        Long siteId,

        @JsonProperty("siteName")
        String siteName,

        @JsonProperty("parameters")
        Map<String, String> parameters,

        @JsonProperty("scheduledFrequency")
        String scheduledFrequency,

        @JsonProperty("createdAt")
        LocalDateTime createdAt,

        @JsonProperty("updatedAt")
        LocalDateTime updatedAt,

        @JsonProperty("createdBy")
        String createdBy
) {}