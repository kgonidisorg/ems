package com.ecogrid.ems.device.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for caching in the device service
 * Enables caching for telemetry data and device status
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure cache manager for in-memory caching
     * In production, this should be replaced with Redis or another distributed cache
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "deviceTelemetry",
            "deviceStatus",
            "siteOverview"
        );
    }
}