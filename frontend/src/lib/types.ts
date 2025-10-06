// User and Authentication Types
export interface User {
  id: number;
  username: string;
  email: string;
  role: 'ADMIN' | 'OPERATOR' | 'VIEWER';
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
  expiresIn: number;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role?: 'ADMIN' | 'OPERATOR' | 'VIEWER';
}

// Device Types
export interface Device {
  id: number;
  siteId: number;
  type: string;
  model: string;
  status: 'ONLINE' | 'OFFLINE' | 'MAINTENANCE' | 'ERROR';
  lastSeen: string;
  configuration: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
}

export interface Site {
  id: number;
  name: string;
  description?: string;
  locationLat: number;
  locationLng: number;
  capacityMw: number;
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE';
  timezone?: string;
  address?: string;
  contactPerson?: string;
  contactEmail?: string;
  contactPhone?: string;
  deviceCount?: number;
  createdAt: string;
  updatedAt: string;
  devices?: Device[];
}

// Site Overview Types (from backend SiteOverviewDTO)
export interface SiteOption {
  id: number;
  name: string;
}

export interface LatestTelemetryData {
  timestamp: string;
  telemetryType: string;
  data: Record<string, unknown>;
}

export interface SiteDevice {
  id: number;
  serialNumber: string;
  name: string;
  deviceType: string;
  model: string;
  manufacturer: string;
  status: 'ONLINE' | 'OFFLINE' | 'MAINTENANCE' | 'ERROR';
  lastCommunication: string | null;
  latestTelemetry: LatestTelemetryData | null;
}

export interface EnergyMetrics {
  totalPowerKw: number;
  totalEnergyKwh: number;
  averageVoltage: number;
  averageCurrent: number;
}

export interface SiteSummary {
  totalDevices: number;
  onlineDevices: number;
  offlineDevices: number;
  alertingDevices: number;
  lastTelemetryUpdate: string | null;
  energyMetrics: EnergyMetrics;
}

export interface SiteOverview {
  id: number;
  name: string;
  description: string;
  locationLat: number;
  locationLng: number;
  capacityMw: number;
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE';
  timezone: string;
  address: string;
  lastUpdated: string;
  devices: SiteDevice[];
  summary: SiteSummary;
  contactPerson: string | null;
  contactEmail: string | null;
  contactPhone: string | null;
}

// Analytics Types
export interface EnergyReading {
  id: number;
  deviceId: number;
  timestamp: string;
  powerKw: number;
  energyKwh: number;
  voltage: number;
  current: number;
}

export interface DashboardStats {
  totalSites: number;
  totalCapacity: number;
  carbonSaved: number;
  gridRevenue: number;
  renewableMix: number;
  activeServices: number;
}

// Backend Analytics Response Types
export interface TimeSeriesDataPoint {
  timestamp: string;
  energyConsumed: number;
  energyGenerated: number;
  carbonSaved: number;
  costSavings: number;
}

export interface DashboardResponse {
  totalEnergyConsumed: number;
  totalEnergyGenerated: number;
  carbonFootprintReduced: number;
  costSavings: number;
  activeSites: number;
  activeDevices: number;
  averageEfficiency: number;
  timeSeriesData: TimeSeriesDataPoint[];
  siteBreakdown: Record<string, number>;
  deviceTypeBreakdown: Record<string, number>;
  lastUpdated: string;
}

export interface EnergyConsumptionResponse {
  totalConsumption: number;
  averageConsumption: number;
  peakConsumption: number;
  aggregation: string;
  periodStart: string;
  periodEnd: string;
  dataPoints: Array<{
    timestamp: string;
    consumption: number;
  }>;
}

export interface CarbonFootprintResponse {
  totalCarbon: number;
  averageCarbon: number;
  peakCarbon: number;
  periodStart: string;
  periodEnd: string;
  dataPoints: Array<{
    timestamp: string;
    carbon: number;
  }>;
}

export interface FinancialMetricsResponse {
  totalRevenue: number;
  totalCosts: number;
  netProfit: number;
  roi: number;
  periodStart: string;
  periodEnd: string;
  dataPoints: Array<{
    timestamp: string;
    revenue: number;
    costs: number;
    profit: number;
  }>;
}

// Alert Types
export interface Alert {
  id: number;
  deviceId: number;
  type: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  message: string;
  acknowledged: boolean;
  createdAt: string;
  resolvedAt?: string;
  device?: Device;
}

export interface NotificationRule {
  id: number;
  userId: number;
  alertType: string;
  minSeverity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  active: boolean;
  createdAt: string;
}

// WebSocket Types
export interface WebSocketMessage {
  type: 'ALERT' | 'DEVICE_STATUS' | 'ENERGY_UPDATE';
  data: Alert | Device | EnergyReading;
  timestamp: string;
}

// API Response wrapper
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface ApiError {
  message: string;
  code?: string;
  details?: Record<string, unknown>;
}