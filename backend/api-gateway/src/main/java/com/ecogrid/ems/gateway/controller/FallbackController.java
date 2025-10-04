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
     * Fallback for device service
     */
    @GetMapping("/device-service")
    @PostMapping("/device-service")
    public ResponseEntity<?> deviceServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Device service is currently unavailable",
                        "message", "Please try again later",
                        "service", "device-service",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    /**
     * Fallback for analytics service
     */
    @GetMapping("/analytics-service")
    @PostMapping("/analytics-service")
    public ResponseEntity<?> analyticsServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Analytics service is currently unavailable",
                        "message", "Please try again later",
                        "service", "analytics-service",
                        "timestamp", System.currentTimeMillis()
                ));
    }

    /**
     * Fallback for notification service
     */
    @GetMapping("/notification-service")
    @PostMapping("/notification-service")
    public ResponseEntity<?> notificationServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Notification service is currently unavailable",
                        "message", "Please try again later",
                        "service", "notification-service",
                        "timestamp", System.currentTimeMillis()
                ));
    }
}