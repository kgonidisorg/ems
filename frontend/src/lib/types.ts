// User and Authentication Types
export interface User {
    id: number;
    username: string;
    email: string;
    role: "ADMIN" | "OPERATOR" | "VIEWER";
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
    role?: "ADMIN" | "OPERATOR" | "VIEWER";
}

// Device Types
export interface Device {
    id: number;
    siteId: number;
    type: string;
    model: string;
    status: "ONLINE" | "OFFLINE" | "MAINTENANCE" | "ERROR";
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
    status: "ACTIVE" | "INACTIVE" | "MAINTENANCE";
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

export interface SiteOption {
    id: number;
    name: string;
}

// Site Overview Types (matches backend payload)
export interface SiteOverview {
    id: number;
    name: string;
    description: string;
    locationLat: number;
    locationLng: number;
    capacityMw: number;
    status: string;
    timezone: string;
    address: string;
    contactPerson: string;
    contactEmail: string;
    contactPhone: string;
    lastUpdated: string;
    devices: SiteDevice[];
    summary: SiteSummary;
}

export interface SiteDevice {
    id: number;
    serialNumber: string;
    name: string;
    deviceType: string;
    model: string;
    manufacturer: string;
    status: string;
    lastCommunication: string | null;
    latestTelemetry: LatestTelemetry | null;
}

export interface LatestTelemetry {
    timestamp: string;
    telemetryType: string;
    data: TelemetryData;
}

export type TelemetryData =
    | BMSTelemetryData
    | SolarArrayTelemetryData
    | EVChargerTelemetryData;

export interface BMSTelemetryData {
    efficiency: number;
    soc: number;
    alarms: unknown[];
    warnings: unknown[];
    nominalCapacity: number;
    qualityIndicators: unknown;
    moduleTemperatures: number[];
    deviceId: number;
    voltage: number;
    lastMaintenance: number[];
    chargeRate: number;
    current: number;
    healthStatus: string;
    temperature: number;
    cycleCount: number;
    remainingCapacity: number;
    timestamp: string;
}

export interface SolarArrayTelemetryData {
    inverterEfficiency: number | null;
    lastCleaning: string | null;
    energyYield: number | null;
    alarms: unknown;
    currentOutput: number | null;
    panelTemperature: number | null;
    qualityIndicators: unknown;
    deviceId: number;
    ambientTemperature: number | null;
    stringData: unknown;
    inverterStatus: unknown;
    energyYieldTotal: number | null;
    systemEfficiency: number | null;
    irradiance: number;
    performanceRatio: number | null;
    windSpeed: number | null;
    timestamp: string;
}

export interface EVChargerTelemetryData {
    networkConnectivity: boolean;
    powerDelivered: number;
    avgSessionDuration: number;
    utilizationRate: number;
    qualityIndicators: unknown;
    deviceId: number;
    faults: number;
    uptime: number;
    totalSessions: number;
    revenue: number;
    paymentSystemStatus: string;
    chargerData: ChargerData[];
    activeSessions: number;
    energyDelivered: number;
    timestamp: string;
}

export interface ChargerData {
    chargerId: string;
    status: string;
    sessionId: string;
    powerOutput: number;
    sessionDuration: number;
    energyDelivered: number;
    connectorType: string;
}

export interface SiteSummary {
    totalDevices: number;
    onlineDevices: number;
    offlineDevices: number;
    alertingDevices: number;
    lastTelemetryUpdate: string;
    energyMetrics: EnergyMetrics;
}

export interface EnergyMetrics {
    totalPowerKw: number;
    totalEnergyKwh: number;
    averageVoltage: number;
    averageCurrent: number;
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
    severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
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
    minSeverity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
    active: boolean;
    createdAt: string;
}

// WebSocket Types
export interface WebSocketMessage {
    type: "ALERT" | "DEVICE_STATUS" | "ENERGY_UPDATE";
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
