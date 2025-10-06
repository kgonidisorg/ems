package com.ecogrid.ems.shared.service;

import com.ecogrid.ems.shared.dto.websocket.EMSWebSocketDelta;
import com.ecogrid.ems.shared.dto.websocket.EMSWebSocketMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for generating delta-based WebSocket updates
 * Tracks previous state and generates only changed fields
 */
@Service
public class WebSocketDeltaService {
    
    // Cache to store previous state for each site
    private final Map<String, EMSWebSocketMessage> previousStates = new ConcurrentHashMap<>();
    
    // Threshold for numerical changes to trigger an update
    private static final double CHANGE_THRESHOLD = 0.01; // 1% change
    
    /**
     * Generate delta update by comparing current state with previous state
     */
    public EMSWebSocketDelta generateDelta(String siteId, EMSWebSocketMessage currentState) {
        EMSWebSocketMessage previousState = previousStates.get(siteId);
        
        if (previousState == null) {
            // First time - send full update
            previousStates.put(siteId, currentState);
            return convertToFullDelta(currentState);
        }
        
        EMSWebSocketDelta delta = EMSWebSocketDelta.builder()
            .siteId(siteId)
            .timestamp(LocalDateTime.now())
            .type(EMSWebSocketDelta.MessageType.DELTA_UPDATE)
            .build();
        
        boolean hasChanges = false;
        
        // Compare site info
        EMSWebSocketDelta.SiteInfoDelta siteInfoDelta = compareSiteInfo(
            previousState.getSiteInfo(), currentState.getSiteInfo()
        );
        if (siteInfoDelta != null) {
            delta.setSiteInfo(siteInfoDelta);
            hasChanges = true;
        }
        
        // Compare battery system
        EMSWebSocketDelta.BatterySystemDelta batteryDelta = compareBatterySystem(
            previousState.getBatterySystem(), currentState.getBatterySystem()
        );
        if (batteryDelta != null) {
            delta.setBatterySystem(batteryDelta);
            hasChanges = true;
        }
        
        // Compare solar array
        EMSWebSocketDelta.SolarArrayDelta solarDelta = compareSolarArray(
            previousState.getSolarArray(), currentState.getSolarArray()
        );
        if (solarDelta != null) {
            delta.setSolarArray(solarDelta);
            hasChanges = true;
        }
        
        // Compare EV charger
        EMSWebSocketDelta.EVChargerDelta evChargerDelta = compareEVCharger(
            previousState.getEvCharger(), currentState.getEvCharger()
        );
        if (evChargerDelta != null) {
            delta.setEvCharger(evChargerDelta);
            hasChanges = true;
        }
        
        // Compare operational data
        EMSWebSocketDelta.OperationalDataDelta operationalDelta = compareOperationalData(
            previousState.getOperationalData(), currentState.getOperationalData()
        );
        if (operationalDelta != null) {
            delta.setOperationalData(operationalDelta);
            hasChanges = true;
        }
        
        // Update cache with current state
        if (hasChanges) {
            previousStates.put(siteId, currentState);
            return delta;
        }
        
        return null; // No changes, don't send update
    }
    
    /**
     * Convert full message to delta for initial load
     */
    private EMSWebSocketDelta convertToFullDelta(EMSWebSocketMessage fullMessage) {
        return EMSWebSocketDelta.builder()
            .siteId(fullMessage.getSiteId())
            .timestamp(fullMessage.getTimestamp())
            .type(EMSWebSocketDelta.MessageType.FULL_UPDATE)
            .siteInfo(convertSiteInfo(fullMessage.getSiteInfo()))
            .batterySystem(convertBatterySystem(fullMessage.getBatterySystem()))
            .solarArray(convertSolarArray(fullMessage.getSolarArray()))
            .evCharger(convertEVCharger(fullMessage.getEvCharger()))
            .operationalData(convertOperationalData(fullMessage.getOperationalData()))
            .build();
    }
    
    /**
     * Compare site info sections
     */
    private EMSWebSocketDelta.SiteInfoDelta compareSiteInfo(
            EMSWebSocketMessage.SiteInfoData previous, 
            EMSWebSocketMessage.SiteInfoData current) {
        
        if (previous == null && current == null) return null;
        if (previous == null) return convertSiteInfo(current);
        if (current == null) return null;
        
        Map<String, Object> changedFields = new HashMap<>();
        EMSWebSocketDelta.SiteInfoDelta.SiteInfoDeltaBuilder builder = 
            EMSWebSocketDelta.SiteInfoDelta.builder();
        
        if (!equals(previous.getLocation(), current.getLocation())) {
            builder.location(current.getLocation());
            changedFields.put("location", current.getLocation());
        }
        
        if (!equals(previous.getGeo(), current.getGeo())) {
            builder.geo(current.getGeo());
            changedFields.put("geo", current.getGeo());
        }
        
        if (!equals(previous.getContact(), current.getContact())) {
            builder.contact(current.getContact());
            changedFields.put("contact", current.getContact());
        }
        
        if (!equals(previous.getEmail(), current.getEmail())) {
            builder.email(current.getEmail());
            changedFields.put("email", current.getEmail());
        }
        
        if (!equals(previous.getWebsite(), current.getWebsite())) {
            builder.website(current.getWebsite());
            changedFields.put("website", current.getWebsite());
        }
        
        if (!equals(previous.getStatus(), current.getStatus())) {
            builder.status(current.getStatus());
            changedFields.put("status", current.getStatus());
        }
        
        if (!equals(previous.getLastUpdated(), current.getLastUpdated())) {
            builder.lastUpdated(current.getLastUpdated());
            changedFields.put("lastUpdated", current.getLastUpdated());
        }
        
        if (changedFields.isEmpty()) {
            return null;
        }
        
        return builder.changedFields(changedFields).build();
    }
    
    /**
     * Compare battery system sections
     */
    private EMSWebSocketDelta.BatterySystemDelta compareBatterySystem(
            EMSWebSocketMessage.BatterySystemData previous, 
            EMSWebSocketMessage.BatterySystemData current) {
        
        if (previous == null && current == null) return null;
        if (previous == null) return convertBatterySystem(current);
        if (current == null) return null;
        
        Map<String, Object> changedFields = new HashMap<>();
        EMSWebSocketDelta.BatterySystemDelta.BatterySystemDeltaBuilder builder = 
            EMSWebSocketDelta.BatterySystemDelta.builder();
        
        if (hasSignificantChange(previous.getSoc(), current.getSoc())) {
            builder.soc(current.getSoc());
            changedFields.put("soc", current.getSoc());
        }
        
        if (hasSignificantChange(previous.getChargeRate(), current.getChargeRate())) {
            builder.chargeRate(current.getChargeRate());
            changedFields.put("chargeRate", current.getChargeRate());
        }
        
        if (hasSignificantChange(previous.getTemperature(), current.getTemperature())) {
            builder.temperature(current.getTemperature());
            changedFields.put("temperature", current.getTemperature());
        }
        
        if (hasSignificantChange(previous.getRemainingCapacity(), current.getRemainingCapacity())) {
            builder.remainingCapacity(current.getRemainingCapacity());
            changedFields.put("remainingCapacity", current.getRemainingCapacity());
        }
        
        if (!equals(previous.getHealthStatus(), current.getHealthStatus())) {
            builder.healthStatus(current.getHealthStatus());
            changedFields.put("healthStatus", current.getHealthStatus());
        }
        
        if (hasSignificantChange(previous.getEfficiency(), current.getEfficiency())) {
            builder.efficiency(current.getEfficiency());
            changedFields.put("efficiency", current.getEfficiency());
        }
        
        // Add other battery fields...
        
        if (changedFields.isEmpty()) {
            return null;
        }
        
        return builder.changedFields(changedFields).build();
    }
    
    /**
     * Compare solar array sections
     */
    private EMSWebSocketDelta.SolarArrayDelta compareSolarArray(
            EMSWebSocketMessage.SolarArrayData previous, 
            EMSWebSocketMessage.SolarArrayData current) {
        
        if (previous == null && current == null) return null;
        if (previous == null) return convertSolarArray(current);
        if (current == null) return null;
        
        Map<String, Object> changedFields = new HashMap<>();
        EMSWebSocketDelta.SolarArrayDelta.SolarArrayDeltaBuilder builder = 
            EMSWebSocketDelta.SolarArrayDelta.builder();
        
        if (hasSignificantChange(previous.getCurrentOutput(), current.getCurrentOutput())) {
            builder.currentOutput(current.getCurrentOutput());
            changedFields.put("currentOutput", current.getCurrentOutput());
        }
        
        if (hasSignificantChange(previous.getEnergyYield(), current.getEnergyYield())) {
            builder.energyYield(current.getEnergyYield());
            changedFields.put("energyYield", current.getEnergyYield());
        }
        
        if (hasSignificantChange(previous.getPanelTemperature(), current.getPanelTemperature())) {
            builder.panelTemperature(current.getPanelTemperature());
            changedFields.put("panelTemperature", current.getPanelTemperature());
        }
        
        if (hasSignificantChange(previous.getIrradiance(), current.getIrradiance())) {
            builder.irradiance(current.getIrradiance());
            changedFields.put("irradiance", current.getIrradiance());
        }
        
        if (hasSignificantChange(previous.getInverterEfficiency(), current.getInverterEfficiency())) {
            builder.inverterEfficiency(current.getInverterEfficiency());
            changedFields.put("inverterEfficiency", current.getInverterEfficiency());
        }
        
        // Add other solar fields...
        
        if (changedFields.isEmpty()) {
            return null;
        }
        
        return builder.changedFields(changedFields).build();
    }
    
    /**
     * Compare EV charger sections
     */
    private EMSWebSocketDelta.EVChargerDelta compareEVCharger(
            EMSWebSocketMessage.EVChargerData previous, 
            EMSWebSocketMessage.EVChargerData current) {
        
        if (previous == null && current == null) return null;
        if (previous == null) return convertEVCharger(current);
        if (current == null) return null;
        
        Map<String, Object> changedFields = new HashMap<>();
        EMSWebSocketDelta.EVChargerDelta.EVChargerDeltaBuilder builder = 
            EMSWebSocketDelta.EVChargerDelta.builder();
        
        if (!equals(previous.getActiveSessions(), current.getActiveSessions())) {
            builder.activeSessions(current.getActiveSessions());
            changedFields.put("activeSessions", current.getActiveSessions());
        }
        
        if (!equals(previous.getAvailablePorts(), current.getAvailablePorts())) {
            builder.availablePorts(current.getAvailablePorts());
            changedFields.put("availablePorts", current.getAvailablePorts());
        }
        
        if (hasSignificantChange(previous.getPowerDelivered(), current.getPowerDelivered())) {
            builder.powerDelivered(current.getPowerDelivered());
            changedFields.put("powerDelivered", current.getPowerDelivered());
        }
        
        if (hasSignificantChange(previous.getRevenue(), current.getRevenue())) {
            builder.revenue(current.getRevenue());
            changedFields.put("revenue", current.getRevenue());
        }
        
        if (!equals(previous.getFaults(), current.getFaults())) {
            builder.faults(current.getFaults());
            changedFields.put("faults", current.getFaults());
        }
        
        // Add other EV charger fields...
        
        if (changedFields.isEmpty()) {
            return null;
        }
        
        return builder.changedFields(changedFields).build();
    }
    
    /**
     * Compare operational data sections
     */
    private EMSWebSocketDelta.OperationalDataDelta compareOperationalData(
            EMSWebSocketMessage.OperationalData previous, 
            EMSWebSocketMessage.OperationalData current) {
        
        if (previous == null && current == null) return null;
        if (previous == null) return convertOperationalData(current);
        if (current == null) return null;
        
        Map<String, Object> changedFields = new HashMap<>();
        EMSWebSocketDelta.OperationalDataDelta.OperationalDataDeltaBuilder builder = 
            EMSWebSocketDelta.OperationalDataDelta.builder();
        
        if (!equals(previous.getTotalDevices(), current.getTotalDevices())) {
            builder.totalDevices(current.getTotalDevices());
            changedFields.put("totalDevices", current.getTotalDevices());
        }
        
        if (!equals(previous.getOnlineDevices(), current.getOnlineDevices())) {
            builder.onlineDevices(current.getOnlineDevices());
            changedFields.put("onlineDevices", current.getOnlineDevices());
        }
        
        if (!equals(previous.getOfflineDevices(), current.getOfflineDevices())) {
            builder.offlineDevices(current.getOfflineDevices());
            changedFields.put("offlineDevices", current.getOfflineDevices());
        }
        
        if (!equals(previous.getFaultDevices(), current.getFaultDevices())) {
            builder.faultDevices(current.getFaultDevices());
            changedFields.put("faultDevices", current.getFaultDevices());
        }
        
        if (!equals(previous.getTotalActiveAlerts(), current.getTotalActiveAlerts())) {
            builder.totalActiveAlerts(current.getTotalActiveAlerts());
            changedFields.put("totalActiveAlerts", current.getTotalActiveAlerts());
        }
        
        if (hasSignificantChange(previous.getSystemUptime(), current.getSystemUptime())) {
            builder.systemUptime(current.getSystemUptime());
            changedFields.put("systemUptime", current.getSystemUptime());
        }
        
        if (!equals(previous.getNetworkStatus(), current.getNetworkStatus())) {
            builder.networkStatus(current.getNetworkStatus());
            changedFields.put("networkStatus", current.getNetworkStatus());
        }
        
        if (changedFields.isEmpty()) {
            return null;
        }
        
        return builder.changedFields(changedFields).build();
    }
    
    // Conversion methods for full delta
    private EMSWebSocketDelta.SiteInfoDelta convertSiteInfo(EMSWebSocketMessage.SiteInfoData siteInfo) {
        if (siteInfo == null) return null;
        return EMSWebSocketDelta.SiteInfoDelta.builder()
            .location(siteInfo.getLocation())
            .geo(siteInfo.getGeo())
            .contact(siteInfo.getContact())
            .email(siteInfo.getEmail())
            .website(siteInfo.getWebsite())
            .status(siteInfo.getStatus())
            .lastUpdated(siteInfo.getLastUpdated())
            .build();
    }
    
    private EMSWebSocketDelta.BatterySystemDelta convertBatterySystem(EMSWebSocketMessage.BatterySystemData batterySystem) {
        if (batterySystem == null) return null;
        return EMSWebSocketDelta.BatterySystemDelta.builder()
            .soc(batterySystem.getSoc())
            .chargeRate(batterySystem.getChargeRate())
            .temperature(batterySystem.getTemperature())
            .remainingCapacity(batterySystem.getRemainingCapacity())
            .healthStatus(batterySystem.getHealthStatus())
            .efficiency(batterySystem.getEfficiency())
            .targetBand(batterySystem.getTargetBand())
            .avgModules(batterySystem.getAvgModules())
            .nominalCapacity(batterySystem.getNominalCapacity())
            .cycles(batterySystem.getCycles())
            .build();
    }
    
    private EMSWebSocketDelta.SolarArrayDelta convertSolarArray(EMSWebSocketMessage.SolarArrayData solarArray) {
        if (solarArray == null) return null;
        return EMSWebSocketDelta.SolarArrayDelta.builder()
            .currentOutput(solarArray.getCurrentOutput())
            .energyYield(solarArray.getEnergyYield())
            .panelTemperature(solarArray.getPanelTemperature())
            .irradiance(solarArray.getIrradiance())
            .inverterEfficiency(solarArray.getInverterEfficiency())
            .peakTime(solarArray.getPeakTime())
            .yesterdayComparison(solarArray.getYesterdayComparison())
            .cloudCover(solarArray.getCloudCover())
            .inverterModel(solarArray.getInverterModel())
            .safeOperating(solarArray.getSafeOperating())
            .build();
    }
    
    private EMSWebSocketDelta.EVChargerDelta convertEVCharger(EMSWebSocketMessage.EVChargerData evCharger) {
        if (evCharger == null) return null;
        return EMSWebSocketDelta.EVChargerDelta.builder()
            .activeSessions(evCharger.getActiveSessions())
            .totalPorts(evCharger.getTotalPorts())
            .availablePorts(evCharger.getAvailablePorts())
            .powerDelivered(evCharger.getPowerDelivered())
            .avgSessionDuration(evCharger.getAvgSessionDuration())
            .revenue(evCharger.getRevenue())
            .faults(evCharger.getFaults())
            .uptime(evCharger.getUptime())
            .avgPerSession(evCharger.getAvgPerSession())
            .peakHours(evCharger.getPeakHours())
            .rate(evCharger.getRate())
            .build();
    }
    
    private EMSWebSocketDelta.OperationalDataDelta convertOperationalData(EMSWebSocketMessage.OperationalData operationalData) {
        if (operationalData == null) return null;
        return EMSWebSocketDelta.OperationalDataDelta.builder()
            .totalDevices(operationalData.getTotalDevices())
            .onlineDevices(operationalData.getOnlineDevices())
            .offlineDevices(operationalData.getOfflineDevices())
            .faultDevices(operationalData.getFaultDevices())
            .totalActiveAlerts(operationalData.getTotalActiveAlerts())
            .systemUptime(operationalData.getSystemUptime())
            .networkStatus(operationalData.getNetworkStatus())
            .build();
    }
    
    // Helper methods
    private boolean equals(Object a, Object b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }
    
    private boolean hasSignificantChange(Double previous, Double current) {
        if (previous == null && current == null) return false;
        if (previous == null || current == null) return true;
        
        double percentChange = Math.abs((current - previous) / previous);
        return percentChange >= CHANGE_THRESHOLD;
    }
    
    /**
     * Clear cached state for a site (useful when site is disconnected)
     */
    public void clearSiteState(String siteId) {
        previousStates.remove(siteId);
    }
    
    /**
     * Get current cached state for a site
     */
    public EMSWebSocketMessage getCachedState(String siteId) {
        return previousStates.get(siteId);
    }
}