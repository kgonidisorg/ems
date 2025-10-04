package com.ecogrid.ems.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user registration requests
 */
public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @JsonProperty("email")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @JsonProperty("password")
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        @JsonProperty("firstName")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        @JsonProperty("lastName")
        String lastName,

        @JsonProperty("role")
        String role
) {
}