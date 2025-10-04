package com.ecogrid.ems.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Fallback controller for circuit breaker responses
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * Fallback for device service - GET
     */
    @GetMapping("/device-service")
    public ResponseEntity<?> deviceServiceFallbackGet() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Device service is currently unavailable",
                        "message", "Please try again later",
                        "service", "device-service",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    /**
     * Fallback for device service - POST
     */
    @PostMapping("/device-service")
    public ResponseEntity<?> deviceServiceFallbackPost() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Device service is currently unavailable",
                        "message", "Please try again later",
                        "service", "device-service",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    /**
     * Fallback for analytics service - GET
     */
    @GetMapping("/analytics-service")
    public ResponseEntity<?> analyticsServiceFallbackGet() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Analytics service is currently unavailable",
                        "message", "Please try again later",
                        "service", "analytics-service",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    /**
     * Fallback for analytics service - POST
     */
    @PostMapping("/analytics-service")
    public ResponseEntity<?> analyticsServiceFallbackPost() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Analytics service is currently unavailable",
                        "message", "Please try again later",
                        "service", "analytics-service",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    /**
     * Fallback for notification service - GET
     */
    @GetMapping("/notification-service")
    public ResponseEntity<?> notificationServiceFallbackGet() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Notification service is currently unavailable",
                        "message", "Please try again later",
                        "service", "notification-service",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    /**
     * Fallback for notification service - POST
     */
    @PostMapping("/notification-service")
    public ResponseEntity<?> notificationServiceFallbackPost() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Notification service is currently unavailable",
                        "message", "Please try again later",
                        "service", "notification-service",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    /**
     * Fallback for auth service - GET
     */
    @GetMapping("/auth-service")
    public ResponseEntity<?> authServiceFallbackGet() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Auth service is currently unavailable",
                        "message", "Please try again later",
                        "service", "auth-service",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    /**
     * Fallback for auth service - POST
     */
    @PostMapping("/auth-service")
    public ResponseEntity<?> authServiceFallbackPost() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Auth service is currently unavailable",
                        "message", "Please try again later",
                        "service", "auth-service",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    /**
     * Health check fallback
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Health check service is currently unavailable",
                        "message", "Please try again later",
                        "service", "health",
                        "timestamp", System.currentTimeMillis()
                ));
    }
}