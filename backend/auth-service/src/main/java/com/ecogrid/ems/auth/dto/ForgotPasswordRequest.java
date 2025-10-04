package com.ecogrid.ems.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for forgot password requests
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @JsonProperty("email")
        String email
) {
}