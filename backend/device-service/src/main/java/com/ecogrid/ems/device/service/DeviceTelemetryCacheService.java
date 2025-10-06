package com.ecogrid.ems.device.service;

import com.ecogrid.ems.device.dto.telemetry.BaseTelemetryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for caching the latest telemetry values for each device
 */
@Service
public class DeviceTelemetryCacheService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceTelemetryCacheService.class);

    private final ObjectMapper objectMapper;
    
    // In-memory cache for latest telemetry data per device
    private final Map<Long, CachedTelemetryData> telemetryCache = new ConcurrentHashMap<>();

    public DeviceTelemetryCacheService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Cache the latest telemetry data for a device
     */
    @CachePut(value = "deviceTelemetry", key = "#deviceId")
    public CachedTelemetryData cacheLatestTelemetry(Long deviceId, BaseTelemetryDTO telemetryDTO) {
        logger.debug("Caching latest telemetry for device: {}", deviceId);
        
        CachedTelemetryData cachedData = new CachedTelemetryData(
            deviceId,
            telemetryDTO.getTimestamp(),
            convertTelemetryToMap(telemetryDTO),
            telemetryDTO.getClass().getSimpleName()
        );
        
        telemetryCache.put(deviceId, cachedData);
        logger.debug("Cached telemetry for device {}: {}", deviceId, cachedData);
        
        return cachedData;
    }

    /**
     * Cache the latest telemetry data from a saved DeviceTelemetry entity
     * This preserves all the raw sensor data
     */
    @CachePut(value = "deviceTelemetry", key = "#deviceId")
    public CachedTelemetryData cacheLatestTelemetryFromEntity(Long deviceId, com.ecogrid.ems.device.entity.DeviceTelemetry telemetryEntity) {
        logger.debug("Caching latest telemetry from entity for device: {}", deviceId);
        
        String telemetryType = telemetryEntity.getDevice().getDeviceType().getName();
        Map<String, Object> telemetryData = new HashMap<>();
        
        // Copy the raw telemetry data from the entity
        if (telemetryEntity.getData() != null) {
            telemetryData.putAll(telemetryEntity.getData());
        }
        
        CachedTelemetryData cachedData = new CachedTelemetryData(
            deviceId,
            telemetryEntity.getTimestamp(),
            telemetryData,
            telemetryType
        );
        
        telemetryCache.put(deviceId, cachedData);
        logger.debug("Cached telemetry from entity for device {}: {}", deviceId, cachedData);
        
        return cachedData;
    }

    /**
     * Get the latest cached telemetry for a device
     */
    @Cacheable(value = "deviceTelemetry", key = "#deviceId")
    public CachedTelemetryData getLatestTelemetry(Long deviceId) {
        logger.debug("Retrieving cached telemetry for device: {}", deviceId);
        return telemetryCache.get(deviceId);
    }

    /**
     * Get latest telemetry for multiple devices
     */
    public Map<Long, CachedTelemetryData> getLatestTelemetryForDevices(Iterable<Long> deviceIds) {
        Map<Long, CachedTelemetryData> result = new HashMap<>();
        
        for (Long deviceId : deviceIds) {
            CachedTelemetryData cachedData = getLatestTelemetry(deviceId);
            if (cachedData != null) {
                result.put(deviceId, cachedData);
            }
        }
        
        logger.debug("Retrieved cached telemetry for {} devices", result.size());
        return result;
    }

    /**
     * Convert telemetry DTO to a map of key-value pairs
     */
    private Map<String, Object> convertTelemetryToMap(BaseTelemetryDTO telemetryDTO) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> telemetryMap = objectMapper.convertValue(telemetryDTO, Map.class);
            
            // Remove deviceId and timestamp as they're stored separately
            telemetryMap.remove("deviceId");
            telemetryMap.remove("timestamp");
            
            return telemetryMap;
        } catch (Exception e) {
            logger.warn("Failed to convert telemetry DTO to map for device: {}", telemetryDTO.getDeviceId(), e);
            return new HashMap<>();
        }
    }

    /**
     * Clear cache for a specific device
     */
    public void clearDeviceCache(Long deviceId) {
        telemetryCache.remove(deviceId);
        logger.debug("Cleared cache for device: {}", deviceId);
    }

    /**
     * Clear all cached telemetry data
     */
    public void clearAllCache() {
        telemetryCache.clear();
        logger.info("Cleared all telemetry cache");
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachedDevices", telemetryCache.size());
        stats.put("oldestEntry", telemetryCache.values().stream()
            .map(CachedTelemetryData::getTimestamp)
            .min(LocalDateTime::compareTo)
            .orElse(null));
        stats.put("newestEntry", telemetryCache.values().stream()
            .map(CachedTelemetryData::getTimestamp)
            .max(LocalDateTime::compareTo)
            .orElse(null));
        
        return stats;
    }

    /**
     * Cached telemetry data holder
     */
    public static class CachedTelemetryData {
        private final Long deviceId;
        private final LocalDateTime timestamp;
        private final Map<String, Object> telemetryData;
        private final String telemetryType; // BMS, Solar, EVCharger, etc.

        public CachedTelemetryData(Long deviceId, LocalDateTime timestamp, 
                                 Map<String, Object> telemetryData, String telemetryType) {
            this.deviceId = deviceId;
            this.timestamp = timestamp;
            this.telemetryData = telemetryData;
            this.telemetryType = telemetryType;
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public Map<String, Object> getTelemetryData() {
            return telemetryData;
        }

        public String getTelemetryType() {
            return telemetryType;
        }

        @Override
        public String toString() {
            return "CachedTelemetryData{" +
                    "deviceId=" + deviceId +
                    ", timestamp=" + timestamp +
                    ", telemetryType='" + telemetryType + '\'' +
                    ", dataKeys=" + (telemetryData != null ? telemetryData.keySet() : "null") +
                    '}';
        }
    }
}