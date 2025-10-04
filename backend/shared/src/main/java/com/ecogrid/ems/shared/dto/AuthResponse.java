package com.ecogrid.ems.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for authentication response
 */
public record AuthResponse(
        @JsonProperty("token") String token,

        @JsonProperty("refreshToken") String refreshToken,

        @JsonProperty("expiresIn") long expiresIn,

        @JsonProperty("user") UserInfo user) {
}