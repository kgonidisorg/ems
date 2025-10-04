package com.ecogrid.ems.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for change password requests
 */
public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        @JsonProperty("currentPassword")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @JsonProperty("newPassword")
        String newPassword
) {
}