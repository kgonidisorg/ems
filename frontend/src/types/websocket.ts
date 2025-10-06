// WebSocket message types that match the backend DTOs
export interface EMSWebSocketMessage {
  siteId: string;
  timestamp: string;
  type: "SITE_UPDATE" | "DEVICE_UPDATE" | "ALERT_UPDATE";
  
  siteInfo?: SiteInfoData;
  batterySystem?: BatterySystemData;
  solarArray?: SolarArrayData;
  evCharger?: EVChargerData;
  operationalData?: OperationalData;
  forecast?: ForecastData[];
  schedule?: ScheduleData[];
}

export interface EMSWebSocketDelta {
  siteId: string;
  timestamp: string;
  type: "FULL_UPDATE" | "DELTA_UPDATE" | "ALERT_UPDATE" | "DEVICE_STATUS_UPDATE";
  
  siteInfo?: SiteInfoDelta;
  batterySystem?: BatterySystemDelta;
  solarArray?: SolarArrayDelta;
  evCharger?: EVChargerDelta;
  operationalData?: OperationalDataDelta;
  forecast?: ForecastDelta[];
  schedule?: ScheduleDelta[];
}

export interface SiteInfoData {
  location: string;
  geo: string;
  contact: string;
  email: string;
  website: string;
  status: "ONLINE" | "OFFLINE" | "MAINTENANCE";
  lastUpdated: string;
}

export interface SiteInfoDelta extends SiteInfoData {
  changedFields?: Record<string, unknown>;
}

export interface BatterySystemData {
  soc: number; // State of Charge (%)
  chargeRate: number; // Charge/Discharge Rate (kW)
  temperature: number; // Battery Temperature (°C)
  remainingCapacity: number; // Remaining Capacity (kWh)
  healthStatus: string; // "Good" | "Fair" | "Poor"
  efficiency: number; // Round-Trip Efficiency (%)
  targetBand: { min: number; max: number }; // Target SOC band
  avgModules: number; // Average of 16 modules
  nominalCapacity: number; // Nominal: 1 MWh
  cycles: { current: number; max: number }; // Cycles: 1,200 / 5,000
}

export interface BatterySystemDelta extends BatterySystemData {
  changedFields?: Record<string, unknown>;
}

export interface SolarArrayData {
  currentOutput: number; // Current Output (kW)
  energyYield: number; // Today's Energy Yield (kWh)
  panelTemperature: number; // Panel Temperature (°C)
  irradiance: number; // Irradiance (W/m²)
  inverterEfficiency: number; // Inverter Efficiency (%)
  peakTime: string; // Peak time (e.g., "14:00")
  yesterdayComparison: string; // e.g., "+5% vs. yesterday"
  cloudCover: number; // Cloud cover (%)
  inverterModel: string; // "SMA Sunny Boy 5.0"
  safeOperating: boolean; // < 60°C
}

export interface SolarArrayDelta extends SolarArrayData {
  changedFields?: Record<string, unknown>;
}

export interface EVChargerData {
  activeSessions: number; // Active Sessions (count)
  totalPorts: number; // Total ports (e.g., 10)
  availablePorts: number; // Available ports (e.g., 7)
  powerDelivered: number; // Power Delivered Today (kWh)
  avgSessionDuration: number; // Avg Session Duration (min)
  revenue: number; // Revenue Today ($)
  faults: number; // Faults / Alerts
  uptime: number; // Uptime (%)
  avgPerSession: number; // Avg per session: 40 kWh
  peakHours: string; // Peak hours: "17:00–19:00"
  rate: number; // Rate: $0.15/kWh
}

export interface EVChargerDelta extends EVChargerData {
  changedFields?: Record<string, unknown>;
}

export interface OperationalData {
  totalDevices: number; // Total device count
  onlineDevices: number; // Online device count
  offlineDevices: number; // Offline device count
  faultDevices: number; // Devices with faults
  totalActiveAlerts: number; // Active alerts count
  systemUptime: number; // Overall system uptime (%)
  networkStatus: "ONLINE" | "OFFLINE"; // Live network status
}

export interface OperationalDataDelta extends OperationalData {
  changedFields?: Record<string, unknown>;
}

export interface ForecastData {
  time: string; // "08:00", "10:00", etc.
  irradiance: number; // W/m²
}

export interface ForecastDelta extends ForecastData {
  changeType?: "ADDED" | "UPDATED" | "REMOVED";
}

export interface ScheduleData {
  task: string; // "Battery Charging", "Grid Export", etc.
  time: string; // "08:00"
}

export interface ScheduleDelta extends ScheduleData {
  changeType?: "ADDED" | "UPDATED" | "REMOVED";
}