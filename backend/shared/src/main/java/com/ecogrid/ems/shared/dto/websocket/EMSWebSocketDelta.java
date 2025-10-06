package com.ecogrid.ems.shared.dto.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Delta-based WebSocket message for EMS dashboard updates
 * Only includes fields that have changed since the last update
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EMSWebSocketDelta {
    
    private String siteId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;
    
    private MessageType type;
    
    // Changed fields only - null if no change
    private SiteInfoDelta siteInfo;
    private BatterySystemDelta batterySystem;
    private SolarArrayDelta solarArray;
    private EVChargerDelta evCharger;
    private OperationalDataDelta operationalData;
    private List<ForecastDelta> forecast;
    private List<ScheduleDelta> schedule;
    
    public enum MessageType {
        FULL_UPDATE,        // Initial load with all data
        DELTA_UPDATE,       // Only changed fields
        ALERT_UPDATE,       // Alert-specific update
        DEVICE_STATUS_UPDATE // Device status change
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SiteInfoDelta {
        private String location;
        private String geo;
        private String contact;
        private String email;
        private String website;
        private EMSWebSocketMessage.SiteInfoData.SiteStatus status;
        private LocalDateTime lastUpdated;
        
        // Changed fields indicator
        private Map<String, Object> changedFields;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BatterySystemDelta {
        private Double soc;
        private Double chargeRate;
        private Double temperature;
        private Double remainingCapacity;
        private String healthStatus;
        private Double efficiency;
        private EMSWebSocketMessage.BatterySystemData.TargetBand targetBand;
        private Double avgModules;
        private Double nominalCapacity;
        private EMSWebSocketMessage.BatterySystemData.CycleData cycles;
        
        // Changed fields indicator
        private Map<String, Object> changedFields;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SolarArrayDelta {
        private Double currentOutput;
        private Double energyYield;
        private Double panelTemperature;
        private Double irradiance;
        private Double inverterEfficiency;
        private String peakTime;
        private String yesterdayComparison;
        private Double cloudCover;
        private String inverterModel;
        private Boolean safeOperating;
        
        // Changed fields indicator
        private Map<String, Object> changedFields;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EVChargerDelta {
        private Integer activeSessions;
        private Integer totalPorts;
        private Integer availablePorts;
        private Double powerDelivered;
        private Double avgSessionDuration;
        private Double revenue;
        private Integer faults;
        private Double uptime;
        private Double avgPerSession;
        private String peakHours;
        private Double rate;
        
        // Changed fields indicator
        private Map<String, Object> changedFields;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OperationalDataDelta {
        private Integer totalDevices;
        private Integer onlineDevices;
        private Integer offlineDevices;
        private Integer faultDevices;
        private Integer totalActiveAlerts;
        private Double systemUptime;
        private EMSWebSocketMessage.OperationalData.NetworkStatus networkStatus;
        
        // Changed fields indicator
        private Map<String, Object> changedFields;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ForecastDelta {
        private String time;
        private Double irradiance;
        private String changeType; // "ADDED", "UPDATED", "REMOVED"
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ScheduleDelta {
        private String task;
        private String time;
        private String changeType; // "ADDED", "UPDATED", "REMOVED"
    }
}