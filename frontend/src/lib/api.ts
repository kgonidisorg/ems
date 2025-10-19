import axios, { AxiosInstance, AxiosResponse } from "axios";
import Cookies from "js-cookie";
import { User } from "./types";

const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL || "http://localhost:8080";

// Configuration for different services
const API_CONFIG = {
  API_GATEWAY: `${BASE_URL.replace(/\/$/, "")}/api`,
};

// Token management
export const TokenManager = {
    getToken: (): string | null => {
        return Cookies.get("jwt_token") || localStorage.getItem("jwt_token");
    },

    setToken: (token: string): void => {
        Cookies.set("jwt_token", token, {
            expires: 7,
            secure: true,
            sameSite: "strict",
        });
        localStorage.setItem("jwt_token", token);
    },

    removeToken: (): void => {
        Cookies.remove("jwt_token");
        localStorage.removeItem("jwt_token");
    },

    getUser: (): User | null => {
        const userStr = localStorage.getItem("user");
        return userStr ? JSON.parse(userStr) : null;
    },

    setUser: (user: User): void => {
        localStorage.setItem("user", JSON.stringify(user));
    },

    removeUser: (): void => {
        localStorage.removeItem("user");
    },
};

// Create axios instances for different services
const createApiInstance = (baseURL: string): AxiosInstance => {
    const instance = axios.create({
        baseURL,
        timeout: 10000,
        headers: {
            "Content-Type": "application/json",
        },
    });

    // Request interceptor to add JWT token
    instance.interceptors.request.use(
        (config) => {
            const token = TokenManager.getToken();
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
            return config;
        },
        (error) => {
            return Promise.reject(error);
        }
    );

    // Response interceptor to handle token expiration
    instance.interceptors.response.use(
        (response) => response,
        (error) => {
            if (error.response?.status === 401) {
                // Token expired or invalid
                TokenManager.removeToken();
                TokenManager.removeUser();
                // Since we use modal-based authentication, we'll let the auth context handle this
                // by triggering a re-render that will show the auth modal
                window.dispatchEvent(new CustomEvent("auth:logout"));
            }
            return Promise.reject(error);
        }
    );

    return instance;
};

// API instances
export const apiGateway = createApiInstance(API_CONFIG.API_GATEWAY);

// Re-export types from types.ts
export type { ApiResponse, ApiError } from "./types";

// Generic API request wrapper
export const apiRequest = async <T>(
    request: () => Promise<AxiosResponse<T>>
): Promise<T> => {
    try {
        const response = await request();
        return response.data;
    } catch (error: unknown) {
        const axiosError = error as {
            response?: { data?: { message?: string; code?: string } };
            message?: string;
            code?: string;
        };
        const apiError = {
            message:
                axiosError.response?.data?.message ||
                axiosError.message ||
                "An error occurred",
            code: axiosError.response?.data?.code || axiosError.code,
            details: axiosError.response?.data,
        };
        throw apiError;
    }
};

// =============================================================================
// CACHE SERVICE
// =============================================================================

interface CacheEntry<T> {
    data: T;
    timestamp: number;
    ttl: number;
}

class RequestCache {
    private cache = new Map<string, CacheEntry<unknown>>();
    private pendingRequests = new Map<string, Promise<unknown>>();

    async get<T>(
        key: string,
        fetcher: () => Promise<T>,
        ttl: number = 30000 // 30 seconds default
    ): Promise<T> {
        const cached = this.cache.get(key);
        const now = Date.now();

        // Return cached data if still valid
        if (cached && now - cached.timestamp < cached.ttl) {
            return cached.data as T;
        }

        // Return pending request if already in progress
        if (this.pendingRequests.has(key)) {
            return this.pendingRequests.get(key) as Promise<T>;
        }

        // Start new request
        const promise = fetcher()
            .then((data) => {
                this.cache.set(key, { data, timestamp: now, ttl });
                this.pendingRequests.delete(key);
                return data;
            })
            .catch((error) => {
                this.pendingRequests.delete(key);
                throw error;
            });

        this.pendingRequests.set(key, promise);
        return promise;
    }

    clear(): void {
        this.cache.clear();
        this.pendingRequests.clear();
    }

    invalidate(pattern?: string): void {
        if (pattern) {
            for (const key of this.cache.keys()) {
                if (key.includes(pattern)) {
                    this.cache.delete(key);
                }
            }
        } else {
            this.cache.clear();
        }
    }
}

const requestCache = new RequestCache();

export const CacheManager = {
    clear: () => requestCache.clear(),
    invalidate: (pattern?: string) => requestCache.invalidate(pattern),
    invalidateDashboard: () => requestCache.invalidate("dashboard"),
    invalidateEnergy: () => requestCache.invalidate("energy"),
    invalidateSites: () => requestCache.invalidate("sites"),
    invalidateDevices: () => requestCache.invalidate("devices"),
    getCachedData: <T>(key: string, fetcher: () => Promise<T>, ttl?: number) =>
        requestCache.get(key, fetcher, ttl),
};

// =============================================================================
// API TYPES AND INTERFACES
// =============================================================================

import type {
    SiteOption,
    SiteOverview,
    Site,
    Device,
    DashboardResponse,
    EnergyConsumptionResponse,
    CarbonFootprintResponse,
    FinancialMetricsResponse,
} from "./types";

export interface SitesResponse {
    content: Array<{
        id: number;
        name: string;
        description?: string;
        status: string;
        locationLat?: number;
        locationLng?: number;
        capacityMw?: number;
        address?: string;
        createdAt: string;
        updatedAt: string;
    }>;
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface SiteParams {
    page?: number;
    size?: number;
    sortBy?: "name" | "location" | "capacity";
    sortDir?: "asc" | "desc";
    search?: string;
}

export interface DashboardParams {
    hoursBack?: number;
    siteId?: number;
}

export interface EnergyConsumptionParams {
    startDate?: string;
    endDate?: string;
    siteId?: number;
    aggregation?: "HOURLY" | "DAILY" | "WEEKLY" | "MONTHLY";
}

export interface CarbonFootprintParams {
    startDate?: string;
    endDate?: string;
    siteId?: number;
    aggregation?: "DAILY" | "WEEKLY" | "MONTHLY";
}

export interface FinancialMetricsParams {
    startDate?: string;
    endDate?: string;
    siteId?: number;
    currency?: string;
}

// =============================================================================
// SITE API SERVICE
// =============================================================================

export const SiteService = {
    /**
     * Get all sites for dropdown selection
     * Returns simplified site options with id and name
     */
    async getSiteOptions(): Promise<SiteOption[]> {
        const cacheKey = "site-options";

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    const response = await apiRequest<SitesResponse>(() =>
                        apiGateway.get("/sites?size=100&sort=name,asc")
                    );

                    return response.content.map((site) => ({
                        id: site.id,
                        name: site.name,
                    }));
                } catch (error) {
                    console.error("Error fetching site options:", error);
                    throw new Error("Failed to fetch site options");
                }
            },
            300000 // 5 minute cache
        );
    },

    /**
     * Get detailed site overview with devices and telemetry
     */
    async getSiteOverview(siteId: number): Promise<SiteOverview> {
        const cacheKey = `site-overview-${siteId}`;

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    return await apiRequest<SiteOverview>(() =>
                        apiGateway.get(`/sites/${siteId}/overview`)
                    );
                } catch (error) {
                    console.error(
                        `Error fetching site overview for site ${siteId}:`,
                        error
                    );
                    throw new Error(
                        `Failed to fetch site overview for site ${siteId}`
                    );
                }
            },
            30000 // 30 second cache for real-time data
        );
    },

    /**
     * Get all sites with optional filtering and pagination
     */
    async getSites(params: SiteParams = {}): Promise<Site[]> {
        const cacheKey = `sites-${JSON.stringify(params)}`;

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    const response = await apiRequest(() =>
                        apiGateway.get("/sites", { params })
                    );

                    // Handle paginated response from gateway
                    if (Array.isArray(response)) {
                        return response as Site[];
                    }
                    if (response && Array.isArray(response.content)) {
                        return response.content as Site[];
                    }

                    // Fallback: return empty array
                    return [] as Site[];
                } catch (error) {
                    console.error("Error fetching sites:", error);
                    throw new Error("Failed to fetch sites");
                }
            },
            300000 // 5 minute cache
        );
    },

    /**
     * Get a single site by ID
     */
    async getSiteById(siteId: number): Promise<Site> {
        const cacheKey = `site-${siteId}`;

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    return await apiRequest<Site>(() =>
                        apiGateway.get(`/sites/${siteId}`)
                    );
                } catch (error) {
                    console.error(`Error fetching site ${siteId}:`, error);
                    throw new Error(`Failed to fetch site ${siteId}`);
                }
            },
            300000 // 5 minute cache
        );
    },
};

// =============================================================================
// DEVICE API SERVICE
// =============================================================================

export const DeviceService = {
    /**
     * Get all devices, optionally filtered by site
     */
    async getDevices(siteId?: number): Promise<Device[]> {
        const cacheKey = siteId ? `devices-site-${siteId}` : "devices-all";

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    const url = siteId
                        ? `/devices?siteId=${siteId}`
                        : "/devices";
                    return await apiRequest<Device[]>(() =>
                        apiGateway.get(url)
                    );
                } catch (error) {
                    console.error("Error fetching devices:", error);
                    throw new Error("Failed to fetch devices");
                }
            },
            60000 // 1 minute cache for device data
        );
    },

    /**
     * Get a single device by ID
     */
    async getDeviceById(deviceId: number): Promise<Device> {
        const cacheKey = `device-${deviceId}`;

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    return await apiRequest<Device>(() =>
                        apiGateway.get(`/devices/${deviceId}`)
                    );
                } catch (error) {
                    console.error(`Error fetching device ${deviceId}:`, error);
                    throw new Error(`Failed to fetch device ${deviceId}`);
                }
            },
            60000 // 1 minute cache
        );
    },

    /**
     * Get devices by type
     */
    async getDevicesByType(
        deviceType: string,
        siteId?: number
    ): Promise<Device[]> {
        const cacheKey = siteId
            ? `devices-type-${deviceType}-site-${siteId}`
            : `devices-type-${deviceType}`;

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    const params = new URLSearchParams({ type: deviceType });
                    if (siteId) {
                        params.append("siteId", siteId.toString());
                    }

                    return await apiRequest<Device[]>(() =>
                        apiGateway.get(`/devices?${params.toString()}`)
                    );
                } catch (error) {
                    console.error(
                        `Error fetching devices of type ${deviceType}:`,
                        error
                    );
                    throw new Error(
                        `Failed to fetch devices of type ${deviceType}`
                    );
                }
            },
            60000 // 1 minute cache
        );
    },
};

// =============================================================================
// ANALYTICS API SERVICE
// =============================================================================

export const AnalyticsService = {
    /**
     * Get dashboard data
     */
    async getDashboard(
        params: DashboardParams = {}
    ): Promise<DashboardResponse> {
        const cacheKey = `dashboard-${JSON.stringify(params)}`;

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    return await apiRequest<DashboardResponse>(() =>
                        apiGateway.get("/analytics/dashboard", { params })
                    );
                } catch (error) {
                    console.error("Error fetching dashboard data:", error);
                    throw new Error("Failed to fetch dashboard data");
                }
            },
            30000 // 30 second cache for dashboard
        );
    },

    /**
     * Get energy consumption data
     */
    async getEnergyConsumption(
        params: EnergyConsumptionParams = {}
    ): Promise<EnergyConsumptionResponse> {
        const cacheKey = `energy-consumption-${JSON.stringify(params)}`;

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    return await apiRequest<EnergyConsumptionResponse>(() =>
                        apiGateway.get("/analytics/energy/consumption", {
                            params,
                        })
                    );
                } catch (error) {
                    console.error(
                        "Error fetching energy consumption data:",
                        error
                    );
                    throw new Error("Failed to fetch energy consumption data");
                }
            },
            60000 // 1 minute cache for energy data
        );
    },

    /**
     * Get carbon footprint data
     */
    async getCarbonFootprint(
        params: CarbonFootprintParams = {}
    ): Promise<CarbonFootprintResponse> {
        const cacheKey = `carbon-footprint-${JSON.stringify(params)}`;

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    return await apiRequest<CarbonFootprintResponse>(() =>
                        apiGateway.get("/analytics/carbon/footprint", {
                            params,
                        })
                    );
                } catch (error) {
                    console.error(
                        "Error fetching carbon footprint data:",
                        error
                    );
                    throw new Error("Failed to fetch carbon footprint data");
                }
            },
            300000 // 5 minute cache for carbon data
        );
    },

    /**
     * Get financial metrics
     */
    async getFinancialMetrics(
        params: FinancialMetricsParams = {}
    ): Promise<FinancialMetricsResponse> {
        const cacheKey = `financial-metrics-${JSON.stringify(params)}`;

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    return await apiRequest<FinancialMetricsResponse>(() =>
                        apiGateway.get("/analytics/financial-metrics", {
                            params,
                        })
                    );
                } catch (error) {
                    console.error("Error fetching financial metrics:", error);
                    throw new Error("Failed to fetch financial metrics");
                }
            },
            300000 // 5 minute cache for financial data
        );
    },

    /**
     * Get site-specific analytics summary
     */
    async getSiteAnalytics(
        siteId: number,
        params: Record<string, unknown> = {}
    ): Promise<Record<string, unknown>> {
        const cacheKey = `site-analytics-${siteId}-${JSON.stringify(params)}`;

        return await CacheManager.getCachedData(
            cacheKey,
            async () => {
                try {
                    return await apiRequest(() =>
                        apiGateway.get(`/analytics/sites/${siteId}`, { params })
                    );
                } catch (error) {
                    console.error(
                        `Error fetching analytics for site ${siteId}:`,
                        error
                    );
                    throw new Error(
                        `Failed to fetch analytics for site ${siteId}`
                    );
                }
            },
            60000 // 1 minute cache for site analytics
        );
    },
};
