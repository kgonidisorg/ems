package com.ecogrid.ems.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "mySecretKey"; // Default for development - should be overridden in production
    private long expirationMs = 86400000; // 24 hours in milliseconds
    private long refreshExpirationMs = 604800000; // 7 days in milliseconds
    private String issuer = "ecogrid-ems";

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public void setRefreshExpirationMs(long refreshExpirationMs) {
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}