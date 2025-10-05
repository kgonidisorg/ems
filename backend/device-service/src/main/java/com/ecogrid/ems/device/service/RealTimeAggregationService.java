package com.ecogrid.ems.device.service;

import com.ecogrid.ems.device.entity.Device;
import com.ecogrid.ems.device.entity.DeviceStatusCache;
import com.ecogrid.ems.device.repository.DeviceStatusCacheRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for real-time aggregation of device telemetry data
 */
@Service
@Transactional
public class RealTimeAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeAggregationService.class);

    private final DeviceStatusCacheRepository statusCacheRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RealTimeAggregationService(DeviceStatusCacheRepository statusCacheRepository,
                                     SimpMessagingTemplate messagingTemplate,
                                     KafkaTemplate<String, Object> kafkaTemplate,
                                     ObjectMapper objectMapper) {
        this.statusCacheRepository = statusCacheRepository;
        this.messagingTemplate = messagingTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Listen to device telemetry messages from Kafka and perform real-time aggregations
     */
    @KafkaListener(topics = "device-telemetry", groupId = "aggregation-service")
    public void processDeviceTelemetry(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> telemetryMessage = objectMapper.readValue(message, Map.class);
            
            Long siteId = ((Number) telemetryMessage.get("siteId")).longValue();
            String deviceType = (String) telemetryMessage.get("deviceType");
            @SuppressWarnings("unchecked")
            Map<String, Object> telemetryData = (Map<String, Object>) telemetryMessage.get("telemetry");

            logger.debug("Processing telemetry aggregation for site: {}, device type: {}", siteId, deviceType);

            // Perform device type specific aggregations
            switch (deviceType.toUpperCase()) {
                case "BMS":
                case "BATTERY_STORAGE":
                    aggregateBMSData(siteId, telemetryData);
                    break;
                case "SOLAR_ARRAY":
                case "SOLAR_INVERTER":
                    aggregateSolarData(siteId, telemetryData);
                    break;
                case "EV_CHARGER":
                    aggregateEVChargerData(siteId, telemetryData);
                    break;
                default:
                    logger.debug("Unknown device type for aggregation: {}", deviceType);
            }

            // Update site-level metrics
            updateSiteMetrics(siteId);

            // Publish aggregated data to WebSocket
            publishSiteUpdate(siteId);

        } catch (Exception e) {
            logger.error("Error processing telemetry aggregation", e);
        }
    }

    /**
     * Aggregate BMS telemetry data for site-level metrics
     */
    private void aggregateBMSData(Long siteId, Map<String, Object> telemetryData) {
        try {
            List<DeviceStatusCache> bmsDevices = getBMSDevicesForSite(siteId);
            if (bmsDevices.isEmpty()) {
                return;
            }

            BigDecimal totalCapacity = BigDecimal.ZERO;
            BigDecimal totalRemainingCapacity = BigDecimal.ZERO;
            BigDecimal totalChargeRate = BigDecimal.ZERO;
            BigDecimal avgTemperature = BigDecimal.ZERO;
            BigDecimal avgEfficiency = BigDecimal.ZERO;
            int activeDevices = 0;

            for (DeviceStatusCache deviceCache : bmsDevices) {
                if (deviceCache.getCurrentData() != null) {
                    Map<String, Object> data = deviceCache.getCurrentData();
                    
                    if (data.containsKey("nominalCapacity")) {
                        totalCapacity = totalCapacity.add(getBigDecimalValue(data, "nominalCapacity"));
                    }
                    if (data.containsKey("remainingCapacity")) {
                        totalRemainingCapacity = totalRemainingCapacity.add(getBigDecimalValue(data, "remainingCapacity"));
                    }
                    if (data.containsKey("chargeRate")) {
                        totalChargeRate = totalChargeRate.add(getBigDecimalValue(data, "chargeRate"));
                    }
                    if (data.containsKey("temperature")) {
                        avgTemperature = avgTemperature.add(getBigDecimalValue(data, "temperature"));
                    }
                    if (data.containsKey("efficiency")) {
                        avgEfficiency = avgEfficiency.add(getBigDecimalValue(data, "efficiency"));
                    }
                    
                    if (deviceCache.getStatus() == DeviceStatusCache.DeviceStatus.ONLINE) {
                        activeDevices++;
                    }
                }
            }

            // Calculate averages
            if (bmsDevices.size() > 0) {
                avgTemperature = avgTemperature.divide(BigDecimal.valueOf(bmsDevices.size()), 2, RoundingMode.HALF_UP);
                avgEfficiency = avgEfficiency.divide(BigDecimal.valueOf(bmsDevices.size()), 2, RoundingMode.HALF_UP);
            }

            // Calculate average SOC
            BigDecimal avgSOC = totalCapacity.compareTo(BigDecimal.ZERO) > 0 ? 
                totalRemainingCapacity.divide(totalCapacity, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                BigDecimal.ZERO;

            // Store aggregated BMS data
            Map<String, Object> bmsAggregation = new HashMap<>();
            bmsAggregation.put("totalCapacity", totalCapacity);
            bmsAggregation.put("avgSOC", avgSOC);
            bmsAggregation.put("totalChargeRate", totalChargeRate);
            bmsAggregation.put("avgTemperature", avgTemperature);
            bmsAggregation.put("avgEfficiency", avgEfficiency);
            bmsAggregation.put("activeDevices", activeDevices);
            bmsAggregation.put("offlineDevices", bmsDevices.size() - activeDevices);
            bmsAggregation.put("lastUpdated", LocalDateTime.now());

            // Publish BMS aggregation to Kafka for Analytics Service
            kafkaTemplate.send("site-bms-aggregation", siteId.toString(), bmsAggregation);

        } catch (Exception e) {
            logger.error("Error aggregating BMS data for site: " + siteId, e);
        }
    }

    /**
     * Aggregate Solar Array telemetry data for site-level metrics
     */
    private void aggregateSolarData(Long siteId, Map<String, Object> telemetryData) {
        try {
            List<DeviceStatusCache> solarDevices = getSolarDevicesForSite(siteId);
            if (solarDevices.isEmpty()) {
                return;
            }

            BigDecimal totalOutput = BigDecimal.ZERO;
            BigDecimal totalDailyYield = BigDecimal.ZERO;
            BigDecimal avgEfficiency = BigDecimal.ZERO;
            BigDecimal avgPanelTemp = BigDecimal.ZERO;
            BigDecimal avgIrradiance = BigDecimal.ZERO;
            BigDecimal avgPerformanceRatio = BigDecimal.ZERO;
            int activeStrings = 0;
            int faultedStrings = 0;

            for (DeviceStatusCache deviceCache : solarDevices) {
                if (deviceCache.getCurrentData() != null) {
                    Map<String, Object> data = deviceCache.getCurrentData();
                    
                    if (data.containsKey("currentOutput")) {
                        totalOutput = totalOutput.add(getBigDecimalValue(data, "currentOutput"));
                    }
                    if (data.containsKey("energyYield")) {
                        totalDailyYield = totalDailyYield.add(getBigDecimalValue(data, "energyYield"));
                    }
                    if (data.containsKey("systemEfficiency")) {
                        avgEfficiency = avgEfficiency.add(getBigDecimalValue(data, "systemEfficiency"));
                    }
                    if (data.containsKey("panelTemperature")) {
                        avgPanelTemp = avgPanelTemp.add(getBigDecimalValue(data, "panelTemperature"));
                    }
                    if (data.containsKey("irradiance")) {
                        avgIrradiance = avgIrradiance.add(getBigDecimalValue(data, "irradiance"));
                    }
                    if (data.containsKey("performanceRatio")) {
                        avgPerformanceRatio = avgPerformanceRatio.add(getBigDecimalValue(data, "performanceRatio"));
                    }
                    
                    if (deviceCache.getStatus() == DeviceStatusCache.DeviceStatus.ONLINE) {
                        activeStrings++;
                    } else if (deviceCache.getStatus() == DeviceStatusCache.DeviceStatus.FAULT) {
                        faultedStrings++;
                    }
                }
            }

            // Calculate averages
            if (solarDevices.size() > 0) {
                avgEfficiency = avgEfficiency.divide(BigDecimal.valueOf(solarDevices.size()), 2, RoundingMode.HALF_UP);
                avgPanelTemp = avgPanelTemp.divide(BigDecimal.valueOf(solarDevices.size()), 2, RoundingMode.HALF_UP);
                avgIrradiance = avgIrradiance.divide(BigDecimal.valueOf(solarDevices.size()), 2, RoundingMode.HALF_UP);
                avgPerformanceRatio = avgPerformanceRatio.divide(BigDecimal.valueOf(solarDevices.size()), 2, RoundingMode.HALF_UP);
            }

            // Store aggregated Solar data
            Map<String, Object> solarAggregation = new HashMap<>();
            solarAggregation.put("totalOutput", totalOutput);
            solarAggregation.put("dailyYield", totalDailyYield);
            solarAggregation.put("avgEfficiency", avgEfficiency);
            solarAggregation.put("avgPanelTemp", avgPanelTemp);
            solarAggregation.put("irradiance", avgIrradiance);
            solarAggregation.put("performanceRatio", avgPerformanceRatio);
            solarAggregation.put("activeStrings", activeStrings);
            solarAggregation.put("faultedStrings", faultedStrings);
            solarAggregation.put("lastUpdated", LocalDateTime.now());

            // Publish Solar aggregation to Kafka for Analytics Service
            kafkaTemplate.send("site-solar-aggregation", siteId.toString(), solarAggregation);

        } catch (Exception e) {
            logger.error("Error aggregating Solar data for site: " + siteId, e);
        }
    }

    /**
     * Aggregate EV Charger telemetry data for site-level metrics
     */
    private void aggregateEVChargerData(Long siteId, Map<String, Object> telemetryData) {
        try {
            List<DeviceStatusCache> evChargerDevices = getEVChargerDevicesForSite(siteId);
            if (evChargerDevices.isEmpty()) {
                return;
            }

            Integer totalChargers = evChargerDevices.size();
            Integer totalActiveSessions = 0;
            BigDecimal totalPowerDelivery = BigDecimal.ZERO;
            BigDecimal totalDailyRevenue = BigDecimal.ZERO;
            BigDecimal avgUtilization = BigDecimal.ZERO;
            BigDecimal totalDailyEnergy = BigDecimal.ZERO;
            BigDecimal avgSessionDuration = BigDecimal.ZERO;
            int activeChargers = 0;

            for (DeviceStatusCache deviceCache : evChargerDevices) {
                if (deviceCache.getCurrentData() != null) {
                    Map<String, Object> data = deviceCache.getCurrentData();
                    
                    if (data.containsKey("activeSessions")) {
                        totalActiveSessions += ((Number) data.get("activeSessions")).intValue();
                    }
                    if (data.containsKey("powerDelivered")) {
                        totalPowerDelivery = totalPowerDelivery.add(getBigDecimalValue(data, "powerDelivered"));
                    }
                    if (data.containsKey("revenue")) {
                        totalDailyRevenue = totalDailyRevenue.add(getBigDecimalValue(data, "revenue"));
                    }
                    if (data.containsKey("utilizationRate")) {
                        avgUtilization = avgUtilization.add(getBigDecimalValue(data, "utilizationRate"));
                    }
                    if (data.containsKey("energyDelivered")) {
                        totalDailyEnergy = totalDailyEnergy.add(getBigDecimalValue(data, "energyDelivered"));
                    }
                    if (data.containsKey("avgSessionDuration")) {
                        avgSessionDuration = avgSessionDuration.add(getBigDecimalValue(data, "avgSessionDuration"));
                    }
                    
                    if (deviceCache.getStatus() == DeviceStatusCache.DeviceStatus.ONLINE) {
                        activeChargers++;
                    }
                }
            }

            // Calculate averages
            if (evChargerDevices.size() > 0) {
                avgUtilization = avgUtilization.divide(BigDecimal.valueOf(evChargerDevices.size()), 2, RoundingMode.HALF_UP);
                avgSessionDuration = avgSessionDuration.divide(BigDecimal.valueOf(evChargerDevices.size()), 2, RoundingMode.HALF_UP);
            }

            // Store aggregated EV Charger data
            Map<String, Object> evChargerAggregation = new HashMap<>();
            evChargerAggregation.put("totalChargers", totalChargers);
            evChargerAggregation.put("activeChargers", activeChargers);
            evChargerAggregation.put("activeSessions", totalActiveSessions);
            evChargerAggregation.put("totalPowerDelivery", totalPowerDelivery);
            evChargerAggregation.put("dailyRevenue", totalDailyRevenue);
            evChargerAggregation.put("avgUtilization", avgUtilization);
            evChargerAggregation.put("dailyEnergy", totalDailyEnergy);
            evChargerAggregation.put("avgSessionDuration", avgSessionDuration);
            evChargerAggregation.put("lastUpdated", LocalDateTime.now());

            // Publish EV Charger aggregation to Kafka for Analytics Service
            kafkaTemplate.send("site-evcharger-aggregation", siteId.toString(), evChargerAggregation);

        } catch (Exception e) {
            logger.error("Error aggregating EV Charger data for site: " + siteId, e);
        }
    }

    /**
     * Update overall site metrics and performance indicators
     */
    private void updateSiteMetrics(Long siteId) {
        try {
            List<DeviceStatusCache> allDevices = statusCacheRepository.findBySiteId(siteId);
            if (allDevices.isEmpty()) {
                return;
            }

            int totalDevices = allDevices.size();
            int onlineDevices = (int) allDevices.stream()
                .filter(d -> d.getStatus() == DeviceStatusCache.DeviceStatus.ONLINE)
                .count();
            int faultDevices = (int) allDevices.stream()
                .filter(d -> d.getStatus() == DeviceStatusCache.DeviceStatus.FAULT)
                .count();
            int totalActiveAlerts = allDevices.stream()
                .mapToInt(DeviceStatusCache::getAlertCount)
                .sum();

            // Calculate uptime and availability
            BigDecimal uptime = totalDevices > 0 ? 
                BigDecimal.valueOf(onlineDevices).divide(BigDecimal.valueOf(totalDevices), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;

            // Store site-level metrics
            Map<String, Object> siteMetrics = new HashMap<>();
            siteMetrics.put("siteId", siteId);
            siteMetrics.put("totalDevices", totalDevices);
            siteMetrics.put("onlineDevices", onlineDevices);
            siteMetrics.put("offlineDevices", totalDevices - onlineDevices);
            siteMetrics.put("faultDevices", faultDevices);
            siteMetrics.put("uptime", uptime);
            siteMetrics.put("availability", uptime); // Simplified - same as uptime for now
            siteMetrics.put("totalActiveAlerts", totalActiveAlerts);
            siteMetrics.put("lastUpdated", LocalDateTime.now());

            // Publish site metrics to Kafka for Analytics Service
            kafkaTemplate.send("site-metrics", siteId.toString(), siteMetrics);

        } catch (Exception e) {
            logger.error("Error updating site metrics for site: " + siteId, e);
        }
    }

    /**
     * Publish site update via WebSocket
     */
    private void publishSiteUpdate(Long siteId) {
        try {
            // Create a summary of site data for WebSocket
            Map<String, Object> siteUpdate = new HashMap<>();
            siteUpdate.put("siteId", siteId);
            siteUpdate.put("timestamp", LocalDateTime.now());
            siteUpdate.put("type", "telemetry_update");

            // Send to site-specific WebSocket channel
            messagingTemplate.convertAndSend("/topic/sites/" + siteId + "/dashboard", siteUpdate);
            
        } catch (Exception e) {
            logger.error("Error publishing site update via WebSocket for site: " + siteId, e);
        }
    }

    /**
     * Helper methods to get devices by type for a site
     */
    private List<DeviceStatusCache> getBMSDevicesForSite(Long siteId) {
        return statusCacheRepository.findBySiteId(siteId).stream()
            .filter(d -> {
                Device device = d.getDevice();
                return device.getDeviceType().getName().toUpperCase().contains("BMS") ||
                       device.getDeviceType().getName().toUpperCase().contains("BATTERY");
            })
            .collect(Collectors.toList());
    }

    private List<DeviceStatusCache> getSolarDevicesForSite(Long siteId) {
        return statusCacheRepository.findBySiteId(siteId).stream()
            .filter(d -> {
                Device device = d.getDevice();
                return device.getDeviceType().getName().toUpperCase().contains("SOLAR") ||
                       device.getDeviceType().getName().toUpperCase().contains("INVERTER");
            })
            .collect(Collectors.toList());
    }

    private List<DeviceStatusCache> getEVChargerDevicesForSite(Long siteId) {
        return statusCacheRepository.findBySiteId(siteId).stream()
            .filter(d -> {
                Device device = d.getDevice();
                return device.getDeviceType().getName().toUpperCase().contains("EV") ||
                       device.getDeviceType().getName().toUpperCase().contains("CHARGER");
            })
            .collect(Collectors.toList());
    }

    /**
     * Helper method to safely extract BigDecimal values from telemetry data
     */
    private BigDecimal getBigDecimalValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            logger.warn("Could not convert value to BigDecimal: {}", value);
            return BigDecimal.ZERO;
        }
    }
}