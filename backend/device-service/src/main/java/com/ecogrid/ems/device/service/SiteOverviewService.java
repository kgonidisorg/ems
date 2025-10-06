package com.ecogrid.ems.device.service;

import com.ecogrid.ems.device.dto.site.SiteDeviceDTO;
import com.ecogrid.ems.device.dto.site.SiteOverviewDTO;
import com.ecogrid.ems.device.entity.Device;
import com.ecogrid.ems.device.entity.Site;
import com.ecogrid.ems.device.repository.DeviceRepository;
import com.ecogrid.ems.device.repository.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for site overview operations including devices and telemetry
 */
@Service
public class SiteOverviewService {

    private static final Logger logger = LoggerFactory.getLogger(SiteOverviewService.class);

    private final SiteRepository siteRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceTelemetryCacheService telemetryCacheService;

    public SiteOverviewService(SiteRepository siteRepository, 
                              DeviceRepository deviceRepository,
                              DeviceTelemetryCacheService telemetryCacheService) {
        this.siteRepository = siteRepository;
        this.deviceRepository = deviceRepository;
        this.telemetryCacheService = telemetryCacheService;
    }

    /**
     * Get site overview with all devices and their latest telemetry
     */
    public Optional<SiteOverviewDTO> getSiteOverview(Long siteId) {
        logger.info("Getting site overview for site ID: {}", siteId);

        // Find the site
        Optional<Site> siteOpt = siteRepository.findById(siteId);
        if (siteOpt.isEmpty()) {
            logger.warn("Site not found with ID: {}", siteId);
            return Optional.empty();
        }

        Site site = siteOpt.get();
        
        // Get all devices for the site
        List<Device> devices = deviceRepository.findBySiteId(siteId);
        logger.info("Found {} devices for site: {}", devices.size(), site.getName());

        // Get device IDs for telemetry lookup
        List<Long> deviceIds = devices.stream()
            .map(Device::getId)
            .collect(Collectors.toList());

        // Get latest telemetry for all devices
        Map<Long, DeviceTelemetryCacheService.CachedTelemetryData> telemetryCache = 
            telemetryCacheService.getLatestTelemetryForDevices(deviceIds);

        // Convert devices to DTOs with telemetry
        List<SiteDeviceDTO> deviceDTOs = devices.stream()
            .map(device -> convertToDeviceDTO(device, telemetryCache.get(device.getId())))
            .collect(Collectors.toList());

        // Calculate site summary
        SiteOverviewDTO.SiteSummaryDTO summary = calculateSiteSummary(deviceDTOs, telemetryCache);

        // Create site overview DTO
        SiteOverviewDTO overview = new SiteOverviewDTO(
            site.getId(),
            site.getName(),
            site.getDescription(),
            site.getLocationLat(),
            site.getLocationLng(),
            site.getCapacityMw(),
            site.getStatus(),
            site.getTimezone(),
            site.getAddress(),
            site.getContactPerson(),
            site.getContactEmail(),
            site.getContactPhone(),
            LocalDateTime.now(),
            deviceDTOs,
            summary
        );

        logger.info("Successfully created site overview for site: {} with {} devices", 
            site.getName(), deviceDTOs.size());

        return Optional.of(overview);
    }

    /**
     * Convert Device entity to SiteDeviceDTO with telemetry
     */
    private SiteDeviceDTO convertToDeviceDTO(Device device, 
                                            DeviceTelemetryCacheService.CachedTelemetryData telemetryData) {
        
        SiteDeviceDTO.LatestTelemetryDTO latestTelemetry = null;
        
        if (telemetryData != null) {
            latestTelemetry = new SiteDeviceDTO.LatestTelemetryDTO(
                telemetryData.getTimestamp(),
                telemetryData.getTelemetryType(),
                telemetryData.getTelemetryData()
            );
        }

        return new SiteDeviceDTO(
            device.getId(),
            device.getSerialNumber(),
            device.getName(),
            device.getDeviceType().getName(),
            device.getModel(),
            device.getManufacturer(),
            device.getStatus(),
            device.getLastCommunication(),
            latestTelemetry
        );
    }

    /**
     * Calculate summary statistics for the site
     */
    private SiteOverviewDTO.SiteSummaryDTO calculateSiteSummary(
            List<SiteDeviceDTO> devices, 
            Map<Long, DeviceTelemetryCacheService.CachedTelemetryData> telemetryCache) {

        int totalDevices = devices.size();
        int onlineDevices = (int) devices.stream()
            .filter(device -> device.getStatus() == Device.DeviceStatus.ONLINE)
            .count();
        int offlineDevices = totalDevices - onlineDevices;
        
        // For alerting devices, we'd need to integrate with alert service
        // For now, just count devices with recent communication issues
        int alertingDevices = (int) devices.stream()
            .filter(device -> device.getLastCommunication() != null && 
                device.getLastCommunication().isBefore(LocalDateTime.now().minusMinutes(30)))
            .count();

        // Find the most recent telemetry update
        LocalDateTime lastTelemetryUpdate = telemetryCache.values().stream()
            .map(DeviceTelemetryCacheService.CachedTelemetryData::getTimestamp)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        // Calculate energy metrics from telemetry
        SiteOverviewDTO.EnergyMetricsDTO energyMetrics = calculateEnergyMetrics(telemetryCache);

        return new SiteOverviewDTO.SiteSummaryDTO(
            totalDevices,
            onlineDevices,
            offlineDevices,
            alertingDevices,
            lastTelemetryUpdate,
            energyMetrics
        );
    }

    /**
     * Calculate energy metrics from cached telemetry data
     */
    private SiteOverviewDTO.EnergyMetricsDTO calculateEnergyMetrics(
            Map<Long, DeviceTelemetryCacheService.CachedTelemetryData> telemetryCache) {

        if (telemetryCache.isEmpty()) {
            return new SiteOverviewDTO.EnergyMetricsDTO(0.0, 0.0, 0.0, 0.0);
        }

        double totalPower = 0.0;
        double totalEnergy = 0.0;
        double totalVoltage = 0.0;
        double totalCurrent = 0.0;
        int voltageCount = 0;
        int currentCount = 0;

        for (DeviceTelemetryCacheService.CachedTelemetryData telemetry : telemetryCache.values()) {
            Map<String, Object> data = telemetry.getTelemetryData();
            
            if (data != null) {
                // Extract power values
                Object powerObj = data.get("power");
                if (powerObj instanceof Number) {
                    totalPower += ((Number) powerObj).doubleValue();
                }

                // Extract energy values
                Object energyObj = data.get("energy");
                if (energyObj instanceof Number) {
                    totalEnergy += ((Number) energyObj).doubleValue();
                }

                // Extract voltage values for averaging
                Object voltageObj = data.get("voltage");
                if (voltageObj instanceof Number) {
                    totalVoltage += ((Number) voltageObj).doubleValue();
                    voltageCount++;
                }

                // Extract current values for averaging
                Object currentObj = data.get("current");
                if (currentObj instanceof Number) {
                    totalCurrent += ((Number) currentObj).doubleValue();
                    currentCount++;
                }
            }
        }

        double averageVoltage = voltageCount > 0 ? totalVoltage / voltageCount : 0.0;
        double averageCurrent = currentCount > 0 ? totalCurrent / currentCount : 0.0;

        return new SiteOverviewDTO.EnergyMetricsDTO(
            totalPower,
            totalEnergy,
            averageVoltage,
            averageCurrent
        );
    }
}