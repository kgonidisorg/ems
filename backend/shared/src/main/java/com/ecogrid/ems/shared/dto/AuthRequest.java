package com.ecogrid.ems.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user authentication requests
 */
public record AuthRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email should be valid") @JsonProperty("email") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters long") @JsonProperty("password") String password) {
}