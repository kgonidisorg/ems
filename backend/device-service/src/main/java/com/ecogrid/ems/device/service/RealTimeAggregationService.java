package com.ecogrid.ems.device.service;

import com.ecogrid.ems.device.entity.Device;
import com.ecogrid.ems.device.entity.DeviceStatusCache;
import com.ecogrid.ems.device.entity.Site;
import com.ecogrid.ems.device.repository.DeviceRepository;
import com.ecogrid.ems.device.repository.DeviceStatusCacheRepository;
import com.ecogrid.ems.device.repository.SiteRepository;
import com.ecogrid.ems.shared.dto.websocket.EMSWebSocketDelta;
import com.ecogrid.ems.shared.dto.websocket.EMSWebSocketMessage;
import com.ecogrid.ems.shared.service.WebSocketDeltaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
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
    private final DeviceRepository deviceRepository;
    private final SiteRepository siteRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final WebSocketDeltaService deltaService;

    @Autowired
    public RealTimeAggregationService(DeviceStatusCacheRepository statusCacheRepository,
                                     DeviceRepository deviceRepository,
                                     SiteRepository siteRepository,
                                     SimpMessagingTemplate messagingTemplate,
                                     KafkaTemplate<String, Object> kafkaTemplate,
                                     WebSocketDeltaService deltaService) {
        this.statusCacheRepository = statusCacheRepository;
        this.deviceRepository = deviceRepository;
        this.siteRepository = siteRepository;
        this.messagingTemplate = messagingTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.deltaService = deltaService;
    }

    /**
     * Listen to device telemetry messages from Kafka and perform real-time aggregations
     */
    @KafkaListener(topics = "device-telemetry", groupId = "aggregation-service")
    public void processDeviceTelemetry(@Payload Map<String, Object> telemetryMessage) {
        try {
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
                case "SOLAR ARRAY":
                case "SOLAR_ARRAY":
                case "SOLAR_INVERTER":
                    aggregateSolarData(siteId, telemetryData);
                    break;
                case "EV CHARGER":
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
     * Publish site update via WebSocket using delta updates
     */
    private void publishSiteUpdate(Long siteId) {
        try {
            // Build comprehensive current state message
            EMSWebSocketMessage currentState = buildComprehensiveMessage(siteId);
            
            // Generate delta update
            EMSWebSocketDelta delta = deltaService.generateDelta(siteId.toString(), currentState);
            
            // Only send update if there are changes
            if (delta != null) {
                // Send to site-specific WebSocket channel
                messagingTemplate.convertAndSend("/topic/sites/" + siteId + "/dashboard", delta);
                logger.debug("Published WebSocket delta update for site: {} with type: {}", 
                    siteId, delta.getType());
            } else {
                logger.debug("No significant changes detected for site: {}, skipping WebSocket update", siteId);
            }
            
        } catch (Exception e) {
            logger.error("Error publishing site update via WebSocket for site: " + siteId, e);
        }
    }
    
    /**
     * Build comprehensive EMS message with all current data for a site
     */
    private EMSWebSocketMessage buildComprehensiveMessage(Long siteId) {
        try {
            // Get site information
            EMSWebSocketMessage.SiteInfoData siteInfo = buildSiteInfo(siteId);
            
            // Aggregate device data by type
            List<DeviceStatusCache> bmsDevices = getBMSDevicesForSite(siteId);
            List<DeviceStatusCache> solarDevices = getSolarDevicesForSite(siteId);
            List<DeviceStatusCache> evDevices = getEVChargerDevicesForSite(siteId);
            
            // Build comprehensive message with all sections
            return EMSWebSocketMessage.builder()
                .siteId(siteId.toString())
                .timestamp(LocalDateTime.now())
                .type(EMSWebSocketMessage.MessageType.SITE_UPDATE)
                .siteInfo(siteInfo)
                .batterySystem(aggregateBMSDataForMessage(bmsDevices))
                .solarArray(aggregateSolarDataForMessage(solarDevices))
                .evCharger(aggregateEVDataForMessage(evDevices))
                .operationalData(buildOperationalData(siteId))
                .forecast(getForecastData(siteId))
                .schedule(getScheduleData(siteId))
                .build();
                
        } catch (Exception e) {
            logger.error("Error building comprehensive message for site: " + siteId, e);
            // Return minimal message on error
            return EMSWebSocketMessage.builder()
                .siteId(siteId.toString())
                .timestamp(LocalDateTime.now())
                .type(EMSWebSocketMessage.MessageType.SITE_UPDATE)
                .build();
        }
    }

    /**
     * Build site information data from database
     */
    private EMSWebSocketMessage.SiteInfoData buildSiteInfo(Long siteId) {
        try {
            Optional<Site> siteOpt = siteRepository.findById(siteId);
            if (siteOpt.isEmpty()) {
                logger.warn("Site not found with ID: {}", siteId);
                return null;
            }
            
            Site site = siteOpt.get();
            
            // Map Site.SiteStatus to EMSWebSocketMessage.SiteInfoData.SiteStatus
            EMSWebSocketMessage.SiteInfoData.SiteStatus status;
            switch (site.getStatus()) {
                case ACTIVE:
                    status = EMSWebSocketMessage.SiteInfoData.SiteStatus.ONLINE;
                    break;
                case OFFLINE:
                    status = EMSWebSocketMessage.SiteInfoData.SiteStatus.OFFLINE;
                    break;
                case MAINTENANCE:
                    status = EMSWebSocketMessage.SiteInfoData.SiteStatus.MAINTENANCE;
                    break;
                default:
                    status = EMSWebSocketMessage.SiteInfoData.SiteStatus.OFFLINE;
            }
            
            // Build location string
            String location = site.getAddress() != null ? site.getAddress() : "Location not specified";
            
            // Build geo coordinates string
            String geo = "";
            if (site.getLocationLat() != null && site.getLocationLng() != null) {
                geo = String.format("%.4f° N, %.4f° W", 
                    site.getLocationLat().doubleValue(), 
                    Math.abs(site.getLocationLng().doubleValue()));
            }
            
            return EMSWebSocketMessage.SiteInfoData.builder()
                .location(location)
                .geo(geo)
                .contact(site.getContactPhone() != null ? site.getContactPhone() : "Contact not available")
                .email(site.getContactEmail() != null ? site.getContactEmail() : "Email not available")
                .website("www.ecogrid.com") // Default website
                .status(status)
                .lastUpdated(site.getUpdatedAt() != null ? site.getUpdatedAt() : LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            logger.error("Error building site info for site: " + siteId, e);
            return EMSWebSocketMessage.SiteInfoData.builder()
                .location("Unknown Location")
                .status(EMSWebSocketMessage.SiteInfoData.SiteStatus.OFFLINE)
                .lastUpdated(LocalDateTime.now())
                .build();
        }
    }
    
    /**
     * Aggregate BMS data specifically for WebSocket message
     */
    private EMSWebSocketMessage.BatterySystemData aggregateBMSDataForMessage(List<DeviceStatusCache> bmsDevices) {
        if (bmsDevices.isEmpty()) {
            return EMSWebSocketMessage.BatterySystemData.builder()
                .soc(0.0)
                .chargeRate(0.0)
                .temperature(0.0)
                .remainingCapacity(0.0)
                .healthStatus("Unknown")
                .efficiency(0.0)
                .targetBand(EMSWebSocketMessage.BatterySystemData.TargetBand.builder().min(20.0).max(80.0).build())
                .avgModules(0.0)
                .nominalCapacity(0.0)
                .cycles(EMSWebSocketMessage.BatterySystemData.CycleData.builder().current(0).max(5000).build())
                .build();
        }
        
        try {
            BigDecimal totalCapacity = BigDecimal.ZERO;
            BigDecimal totalRemainingCapacity = BigDecimal.ZERO;
            BigDecimal totalChargeRate = BigDecimal.ZERO;
            BigDecimal avgTemperature = BigDecimal.ZERO;
            BigDecimal avgEfficiency = BigDecimal.ZERO;
            int totalCycles = 0;
            int activeDevices = 0;
            
            for (DeviceStatusCache deviceCache : bmsDevices) {
                if (deviceCache.getCurrentData() != null) {
                    Map<String, Object> data = deviceCache.getCurrentData();
                    
                    totalCapacity = totalCapacity.add(getBigDecimalValue(data, "nominalCapacity"));
                    totalRemainingCapacity = totalRemainingCapacity.add(getBigDecimalValue(data, "remainingCapacity"));
                    totalChargeRate = totalChargeRate.add(getBigDecimalValue(data, "chargeRate"));
                    avgTemperature = avgTemperature.add(getBigDecimalValue(data, "temperature"));
                    avgEfficiency = avgEfficiency.add(getBigDecimalValue(data, "efficiency"));
                    
                    if (data.containsKey("cycleCount")) {
                        totalCycles += ((Number) data.getOrDefault("cycleCount", 0)).intValue();
                    }
                    
                    if (deviceCache.getStatus() == DeviceStatusCache.DeviceStatus.ONLINE) {
                        activeDevices++;
                    }
                }
            }
            
            // Calculate averages
            int deviceCount = bmsDevices.size();
            if (deviceCount > 0) {
                avgTemperature = avgTemperature.divide(BigDecimal.valueOf(deviceCount), 2, RoundingMode.HALF_UP);
                avgEfficiency = avgEfficiency.divide(BigDecimal.valueOf(deviceCount), 2, RoundingMode.HALF_UP);
            }
            
            // Calculate average SOC
            BigDecimal avgSOC = totalCapacity.compareTo(BigDecimal.ZERO) > 0 ? 
                totalRemainingCapacity.divide(totalCapacity, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                BigDecimal.ZERO;
            
            // Determine health status based on efficiency and active devices
            String healthStatus = "Good";
            if (avgEfficiency.doubleValue() < 70) {
                healthStatus = "Poor";
            } else if (avgEfficiency.doubleValue() < 85) {
                healthStatus = "Fair";
            }
            
            return EMSWebSocketMessage.BatterySystemData.builder()
                .soc(avgSOC.doubleValue())
                .chargeRate(totalChargeRate.doubleValue())
                .temperature(avgTemperature.doubleValue())
                .remainingCapacity(totalRemainingCapacity.doubleValue())
                .healthStatus(healthStatus)
                .efficiency(avgEfficiency.doubleValue())
                .targetBand(EMSWebSocketMessage.BatterySystemData.TargetBand.builder().min(20.0).max(80.0).build())
                .avgModules((double) activeDevices)
                .nominalCapacity(totalCapacity.doubleValue())
                .cycles(EMSWebSocketMessage.BatterySystemData.CycleData.builder()
                    .current(totalCycles / Math.max(deviceCount, 1))
                    .max(5000)
                    .build())
                .build();
                
        } catch (Exception e) {
            logger.error("Error aggregating BMS data for message", e);
            return EMSWebSocketMessage.BatterySystemData.builder()
                .soc(0.0)
                .healthStatus("Error")
                .build();
        }
    }
    
    /**
     * Aggregate Solar Array data specifically for WebSocket message
     */
    private EMSWebSocketMessage.SolarArrayData aggregateSolarDataForMessage(List<DeviceStatusCache> solarDevices) {
        if (solarDevices.isEmpty()) {
            return EMSWebSocketMessage.SolarArrayData.builder()
                .currentOutput(0.0)
                .energyYield(0.0)
                .panelTemperature(0.0)
                .irradiance(0.0)
                .inverterEfficiency(0.0)
                .peakTime("N/A")
                .yesterdayComparison("N/A")
                .cloudCover(0.0)
                .inverterModel("Unknown")
                .safeOperating(true)
                .build();
        }
        
        try {
            BigDecimal totalOutput = BigDecimal.ZERO;
            BigDecimal totalDailyYield = BigDecimal.ZERO;
            BigDecimal avgEfficiency = BigDecimal.ZERO;
            BigDecimal avgPanelTemp = BigDecimal.ZERO;
            BigDecimal avgIrradiance = BigDecimal.ZERO;
            
            for (DeviceStatusCache deviceCache : solarDevices) {
                if (deviceCache.getCurrentData() != null) {
                    Map<String, Object> data = deviceCache.getCurrentData();
                    
                    totalOutput = totalOutput.add(getBigDecimalValue(data, "currentOutput"));
                    totalDailyYield = totalDailyYield.add(getBigDecimalValue(data, "energyYield"));
                    avgEfficiency = avgEfficiency.add(getBigDecimalValue(data, "systemEfficiency"));
                    avgPanelTemp = avgPanelTemp.add(getBigDecimalValue(data, "panelTemperature"));
                    avgIrradiance = avgIrradiance.add(getBigDecimalValue(data, "irradiance"));
                }
            }
            
            // Calculate averages
            int deviceCount = solarDevices.size();
            if (deviceCount > 0) {
                avgEfficiency = avgEfficiency.divide(BigDecimal.valueOf(deviceCount), 2, RoundingMode.HALF_UP);
                avgPanelTemp = avgPanelTemp.divide(BigDecimal.valueOf(deviceCount), 2, RoundingMode.HALF_UP);
                avgIrradiance = avgIrradiance.divide(BigDecimal.valueOf(deviceCount), 2, RoundingMode.HALF_UP);
            }
            
            // Determine peak time (simplified - could be enhanced with actual data)
            String peakTime = "12:00"; // Default noon peak
            
            // Calculate yesterday comparison (simplified)
            String yesterdayComparison = "+5% vs. yesterday"; // Mock data
            
            // Determine cloud cover based on irradiance
            double cloudCover = Math.max(0, 100 - (avgIrradiance.doubleValue() / 10));
            
            return EMSWebSocketMessage.SolarArrayData.builder()
                .currentOutput(totalOutput.doubleValue())
                .energyYield(totalDailyYield.doubleValue())
                .panelTemperature(avgPanelTemp.doubleValue())
                .irradiance(avgIrradiance.doubleValue())
                .inverterEfficiency(avgEfficiency.doubleValue())
                .peakTime(peakTime)
                .yesterdayComparison(yesterdayComparison)
                .cloudCover(cloudCover)
                .inverterModel("SMA Sunny Boy 5.0") // Default model
                .safeOperating(avgPanelTemp.doubleValue() < 60.0)
                .build();
                
        } catch (Exception e) {
            logger.error("Error aggregating Solar data for message", e);
            return EMSWebSocketMessage.SolarArrayData.builder()
                .currentOutput(0.0)
                .safeOperating(false)
                .build();
        }
    }
    
    /**
     * Aggregate EV Charger data specifically for WebSocket message
     */
    private EMSWebSocketMessage.EVChargerData aggregateEVDataForMessage(List<DeviceStatusCache> evDevices) {
        if (evDevices.isEmpty()) {
            return EMSWebSocketMessage.EVChargerData.builder()
                .activeSessions(0)
                .totalPorts(0)
                .availablePorts(0)
                .powerDelivered(0.0)
                .avgSessionDuration(0.0)
                .revenue(0.0)
                .faults(0)
                .uptime(100.0)
                .avgPerSession(0.0)
                .peakHours("N/A")
                .rate(0.15)
                .build();
        }
        
        try {
            int totalPorts = evDevices.size();
            int totalActiveSessions = 0;
            int availablePorts = 0;
            BigDecimal totalPowerDelivered = BigDecimal.ZERO;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal avgSessionDuration = BigDecimal.ZERO;
            int totalFaults = 0;
            int onlineDevices = 0;
            
            for (DeviceStatusCache deviceCache : evDevices) {
                if (deviceCache.getCurrentData() != null) {
                    Map<String, Object> data = deviceCache.getCurrentData();
                    
                    if (data.containsKey("activeSessions")) {
                        totalActiveSessions += ((Number) data.get("activeSessions")).intValue();
                    }
                    
                    totalPowerDelivered = totalPowerDelivered.add(getBigDecimalValue(data, "powerDelivered"));
                    totalRevenue = totalRevenue.add(getBigDecimalValue(data, "revenue"));
                    avgSessionDuration = avgSessionDuration.add(getBigDecimalValue(data, "avgSessionDuration"));
                    
                    if (data.containsKey("faults")) {
                        totalFaults += ((Number) data.getOrDefault("faults", 0)).intValue();
                    }
                    
                    if (deviceCache.getStatus() == DeviceStatusCache.DeviceStatus.ONLINE) {
                        onlineDevices++;
                        // Available if online and no active session
                        if (!data.containsKey("activeSessions") || 
                            ((Number) data.get("activeSessions")).intValue() == 0) {
                            availablePorts++;
                        }
                    }
                }
            }
            
            // Calculate averages
            if (evDevices.size() > 0) {
                avgSessionDuration = avgSessionDuration.divide(BigDecimal.valueOf(evDevices.size()), 2, RoundingMode.HALF_UP);
            }
            
            // Calculate uptime
            double uptime = totalPorts > 0 ? (double) onlineDevices / totalPorts * 100 : 100.0;
            
            // Calculate average per session
            double avgPerSession = totalActiveSessions > 0 ? 
                totalPowerDelivered.doubleValue() / totalActiveSessions : 40.0; // Default 40 kWh
            
            return EMSWebSocketMessage.EVChargerData.builder()
                .activeSessions(totalActiveSessions)
                .totalPorts(totalPorts)
                .availablePorts(availablePorts)
                .powerDelivered(totalPowerDelivered.doubleValue())
                .avgSessionDuration(avgSessionDuration.doubleValue())
                .revenue(totalRevenue.doubleValue())
                .faults(totalFaults)
                .uptime(uptime)
                .avgPerSession(avgPerSession)
                .peakHours("17:00–19:00") // Default peak hours
                .rate(0.15) // Default rate per kWh
                .build();
                
        } catch (Exception e) {
            logger.error("Error aggregating EV Charger data for message", e);
            return EMSWebSocketMessage.EVChargerData.builder()
                .activeSessions(0)
                .uptime(0.0)
                .build();
        }
    }
    
    /**
     * Build operational data from site metrics
     */
    private EMSWebSocketMessage.OperationalData buildOperationalData(Long siteId) {
        try {
            List<DeviceStatusCache> allDevices = statusCacheRepository.findBySiteId(siteId);
            
            int totalDevices = allDevices.size();
            int onlineDevices = (int) allDevices.stream()
                .filter(d -> d.getStatus() == DeviceStatusCache.DeviceStatus.ONLINE)
                .count();
            int offlineDevices = totalDevices - onlineDevices;
            int faultDevices = (int) allDevices.stream()
                .filter(d -> d.getStatus() == DeviceStatusCache.DeviceStatus.FAULT)
                .count();
            int totalActiveAlerts = allDevices.stream()
                .mapToInt(DeviceStatusCache::getAlertCount)
                .sum();
            
            // Calculate system uptime
            double systemUptime = totalDevices > 0 ? (double) onlineDevices / totalDevices * 100 : 100.0;
            
            // Determine network status
            EMSWebSocketMessage.OperationalData.NetworkStatus networkStatus = 
                onlineDevices > 0 ? 
                EMSWebSocketMessage.OperationalData.NetworkStatus.ONLINE : 
                EMSWebSocketMessage.OperationalData.NetworkStatus.OFFLINE;
            
            return EMSWebSocketMessage.OperationalData.builder()
                .totalDevices(totalDevices)
                .onlineDevices(onlineDevices)
                .offlineDevices(offlineDevices)
                .faultDevices(faultDevices)
                .totalActiveAlerts(totalActiveAlerts)
                .systemUptime(systemUptime)
                .networkStatus(networkStatus)
                .build();
                
        } catch (Exception e) {
            logger.error("Error building operational data for site: " + siteId, e);
            return EMSWebSocketMessage.OperationalData.builder()
                .totalDevices(0)
                .systemUptime(0.0)
                .networkStatus(EMSWebSocketMessage.OperationalData.NetworkStatus.OFFLINE)
                .build();
        }
    }
    
    /**
     * Get forecast data (simplified implementation)
     */
    private List<EMSWebSocketMessage.ForecastData> getForecastData(Long siteId) {
        // Simplified forecast data - could be enhanced with weather API integration
        List<EMSWebSocketMessage.ForecastData> forecast = new ArrayList<>();
        
        try {
            // Generate simple forecast for next 8 hours
            String[] times = {"08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00"};
            double[] irradiances = {200.0, 600.0, 900.0, 1000.0, 800.0, 400.0, 100.0, 0.0};
            
            for (int i = 0; i < times.length; i++) {
                forecast.add(EMSWebSocketMessage.ForecastData.builder()
                    .time(times[i])
                    .irradiance(irradiances[i])
                    .build());
            }
            
        } catch (Exception e) {
            logger.error("Error generating forecast data for site: " + siteId, e);
        }
        
        return forecast;
    }
    
    /**
     * Get schedule data (simplified implementation)
     */
    private List<EMSWebSocketMessage.ScheduleData> getScheduleData(Long siteId) {
        // Simplified schedule data - could be enhanced with actual scheduling system
        List<EMSWebSocketMessage.ScheduleData> schedule = new ArrayList<>();
        
        try {
            // Generate sample schedule
            schedule.add(EMSWebSocketMessage.ScheduleData.builder()
                .task("Battery Charging")
                .time("08:00")
                .build());
            schedule.add(EMSWebSocketMessage.ScheduleData.builder()
                .task("Grid Export")
                .time("12:00")
                .build());
            schedule.add(EMSWebSocketMessage.ScheduleData.builder()
                .task("Load Balancing")
                .time("18:00")
                .build());
            
        } catch (Exception e) {
            logger.error("Error generating schedule data for site: " + siteId, e);
        }
        
        return schedule;
    }

    /**
     * Helper methods to get devices by type for a site
     */
    private List<DeviceStatusCache> getBMSDevicesForSite(Long siteId) {
        return statusCacheRepository.findBySiteId(siteId).stream()
            .filter(d -> {
                Optional<Device> deviceOpt = deviceRepository.findById(d.getDeviceId());
                if (deviceOpt.isEmpty()) return false;
                Device device = deviceOpt.get();
                return device.getDeviceType().getName().toUpperCase().contains("BMS") ||
                       device.getDeviceType().getName().toUpperCase().contains("BATTERY");
            })
            .collect(Collectors.toList());
    }

    private List<DeviceStatusCache> getSolarDevicesForSite(Long siteId) {
        return statusCacheRepository.findBySiteId(siteId).stream()
            .filter(d -> {
                Optional<Device> deviceOpt = deviceRepository.findById(d.getDeviceId());
                if (deviceOpt.isEmpty()) return false;
                Device device = deviceOpt.get();
                return device.getDeviceType().getName().toUpperCase().contains("SOLAR") ||
                       device.getDeviceType().getName().toUpperCase().contains("INVERTER");
            })
            .collect(Collectors.toList());
    }

    private List<DeviceStatusCache> getEVChargerDevicesForSite(Long siteId) {
        return statusCacheRepository.findBySiteId(siteId).stream()
            .filter(d -> {
                Optional<Device> deviceOpt = deviceRepository.findById(d.getDeviceId());
                if (deviceOpt.isEmpty()) return false;
                Device device = deviceOpt.get();
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