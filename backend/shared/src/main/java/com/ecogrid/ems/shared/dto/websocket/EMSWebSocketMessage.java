package com.ecogrid.ems.shared.dto.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprehensive WebSocket message structure for EMS dashboard
 * Contains all data sections needed for real-time dashboard updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EMSWebSocketMessage {
    
    private String siteId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime timestamp;
    
    private MessageType type;
    
    // Site Information Section
    private SiteInfoData siteInfo;
    
    // Battery Management System Section
    private BatterySystemData batterySystem;
    
    // Solar Array Section
    private SolarArrayData solarArray;
    
    // EV Charger Station Section
    private EVChargerData evCharger;
    
    // Real-time Operational Data
    private OperationalData operationalData;
    
    // Forecast & Scheduler Data
    private List<ForecastData> forecast;
    private List<ScheduleData> schedule;
    
    public enum MessageType {
        SITE_UPDATE,
        DEVICE_UPDATE,
        ALERT_UPDATE
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SiteInfoData {
        private String location;           // "New York, USA"
        private String geo;               // "40.7128° N, 74.0060° W"
        private String contact;           // "+1 (555) 123-4567"
        private String email;             // "contact@ecogrid.com"
        private String website;           // "www.ecogrid.com"
        private SiteStatus status;
        private LocalDateTime lastUpdated;
        
        public enum SiteStatus {
            ONLINE, OFFLINE, MAINTENANCE
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatterySystemData {
        private Double soc;               // State of Charge (%)
        private Double chargeRate;        // Charge/Discharge Rate (kW)
        private Double temperature;       // Battery Temperature (°C)
        private Double remainingCapacity; // Remaining Capacity (kWh)
        private String healthStatus;      // "Good" | "Fair" | "Poor"
        private Double efficiency;        // Round-Trip Efficiency (%)
        private TargetBand targetBand;    // Target SOC band
        private Double avgModules;        // Average of 16 modules
        private Double nominalCapacity;   // Nominal: 1 MWh
        private CycleData cycles;         // Cycles: 1,200 / 5,000
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TargetBand {
            private Double min;
            private Double max;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CycleData {
            private Integer current;
            private Integer max;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SolarArrayData {
        private Double currentOutput;     // Current Output (kW)
        private Double energyYield;       // Today's Energy Yield (kWh)
        private Double panelTemperature;  // Panel Temperature (°C)
        private Double irradiance;        // Irradiance (W/m²)
        private Double inverterEfficiency; // Inverter Efficiency (%)
        private String peakTime;          // Peak time (e.g., "14:00")
        private String yesterdayComparison; // e.g., "+5% vs. yesterday"
        private Double cloudCover;        // Cloud cover (%)
        private String inverterModel;     // "SMA Sunny Boy 5.0"
        private Boolean safeOperating;    // < 60°C
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EVChargerData {
        private Integer activeSessions;    // Active Sessions (count)
        private Integer totalPorts;        // Total ports (e.g., 10)
        private Integer availablePorts;    // Available ports (e.g., 7)
        private Double powerDelivered;     // Power Delivered Today (kWh)
        private Double avgSessionDuration; // Avg Session Duration (min)
        private Double revenue;            // Revenue Today ($)
        private Integer faults;            // Faults / Alerts
        private Double uptime;             // Uptime (%)
        private Double avgPerSession;      // Avg per session: 40 kWh
        private String peakHours;          // Peak hours: "17:00–19:00"
        private Double rate;               // Rate: $0.15/kWh
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationalData {
        private Integer totalDevices;      // Total device count
        private Integer onlineDevices;     // Online device count
        private Integer offlineDevices;    // Offline device count
        private Integer faultDevices;      // Devices with faults
        private Integer totalActiveAlerts; // Active alerts count
        private Double systemUptime;       // Overall system uptime (%)
        private NetworkStatus networkStatus; // Live network status
        
        public enum NetworkStatus {
            ONLINE, OFFLINE
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastData {
        private String time;              // "08:00", "10:00", etc.
        private Double irradiance;        // W/m²
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleData {
        private String task;              // "Battery Charging", "Grid Export", etc.
        private String time;              // "08:00"
    }
}