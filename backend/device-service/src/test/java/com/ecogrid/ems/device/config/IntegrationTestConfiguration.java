package com.ecogrid.ems.device.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;

/**
 * Test configuration for providing mock beans during integration tests
 */
@TestConfiguration
public class IntegrationTestConfiguration {

    /**
     * Provide a mock SimpMessagingTemplate for WebSocket functionality
     * This prevents the RealTimeAggregationService from failing during tests
     */
    @Bean
    @Primary
    public SimpMessagingTemplate simpMessagingTemplate() {
        return mock(SimpMessagingTemplate.class);
    }
}