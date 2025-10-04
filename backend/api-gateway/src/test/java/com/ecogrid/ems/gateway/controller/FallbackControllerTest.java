package com.ecogrid.ems.gateway.controller;

import com.ecogrid.ems.gateway.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Unit tests for FallbackController
 */
@WebFluxTest(FallbackController.class)
@Import(TestSecurityConfig.class)
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void deviceServiceFallback_GET_ShouldReturnServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback/device-service")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Device service is currently unavailable")
                .jsonPath("$.message").isEqualTo("Please try again later")
                .jsonPath("$.service").isEqualTo("device-service")
                .jsonPath("$.timestamp").isNumber();
    }

    @Test
    void deviceServiceFallback_POST_ShouldReturnServiceUnavailable() {
        webTestClient.post()
                .uri("/fallback/device-service")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Device service is currently unavailable")
                .jsonPath("$.message").isEqualTo("Please try again later")
                .jsonPath("$.service").isEqualTo("device-service")
                .jsonPath("$.timestamp").isNumber();
    }

    @Test
    void analyticsServiceFallback_GET_ShouldReturnServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback/analytics-service")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Analytics service is currently unavailable")
                .jsonPath("$.message").isEqualTo("Please try again later")
                .jsonPath("$.service").isEqualTo("analytics-service")
                .jsonPath("$.timestamp").isNumber();
    }

    @Test
    void analyticsServiceFallback_POST_ShouldReturnServiceUnavailable() {
        webTestClient.post()
                .uri("/fallback/analytics-service")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Analytics service is currently unavailable")
                .jsonPath("$.message").isEqualTo("Please try again later")
                .jsonPath("$.service").isEqualTo("analytics-service")
                .jsonPath("$.timestamp").isNumber();
    }

    @Test
    void notificationServiceFallback_GET_ShouldReturnServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback/notification-service")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Notification service is currently unavailable")
                .jsonPath("$.message").isEqualTo("Please try again later")
                .jsonPath("$.service").isEqualTo("notification-service")
                .jsonPath("$.timestamp").isNumber();
    }

    @Test
    void notificationServiceFallback_POST_ShouldReturnServiceUnavailable() {
        webTestClient.post()
                .uri("/fallback/notification-service")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Notification service is currently unavailable")
                .jsonPath("$.message").isEqualTo("Please try again later")
                .jsonPath("$.service").isEqualTo("notification-service")
                .jsonPath("$.timestamp").isNumber();
    }

    @Test
    void authServiceFallback_GET_ShouldReturnServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback/auth-service")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Auth service is currently unavailable")
                .jsonPath("$.message").isEqualTo("Please try again later")
                .jsonPath("$.service").isEqualTo("auth-service")
                .jsonPath("$.timestamp").isNumber();
    }

    @Test
    void authServiceFallback_POST_ShouldReturnServiceUnavailable() {
        webTestClient.post()
                .uri("/fallback/auth-service")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Auth service is currently unavailable")
                .jsonPath("$.message").isEqualTo("Please try again later")
                .jsonPath("$.service").isEqualTo("auth-service")
                .jsonPath("$.timestamp").isNumber();
    }

    @Test
    void healthFallback_ShouldReturnServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback/health")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Health check service is currently unavailable")
                .jsonPath("$.message").isEqualTo("Please try again later")
                .jsonPath("$.service").isEqualTo("health")
                .jsonPath("$.timestamp").isNumber();
    }
}