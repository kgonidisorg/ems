package com.ecogrid.ems.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for user information
 */
public record UserInfo(
        @JsonProperty("id") Long id,

        @JsonProperty("email") String email,

        @JsonProperty("firstName") String firstName,

        @JsonProperty("lastName") String lastName,

        @JsonProperty("role") String role,

        @JsonProperty("createdAt") String createdAt) {
}