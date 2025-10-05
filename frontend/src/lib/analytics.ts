import { apiGateway, apiRequest } from './api';
import type { 
  DashboardResponse, 
  EnergyConsumptionResponse, 
  CarbonFootprintResponse,
  FinancialMetricsResponse,
  Device,
  Site
} from './types';

// Simple in-memory cache with TTL
interface CacheEntry<T> {
  data: T;
  timestamp: number;
  ttl: number;
}

class RequestCache {
  private cache = new Map<string, CacheEntry<unknown>>();
  private pendingRequests = new Map<string, Promise<unknown>>();

  private getCacheKey(url: string, params: Record<string, unknown> = {}): string {
    return `${url}?${JSON.stringify(params)}`;
  }

  async get<T>(
    key: string, 
    fetcher: () => Promise<T>, 
    ttl: number = 30000 // 30 seconds default
  ): Promise<T> {
    const cached = this.cache.get(key);
    const now = Date.now();

    // Return cached data if still valid
    if (cached && (now - cached.timestamp) < cached.ttl) {
      return cached.data as T;
    }

    // Return pending request if already in progress
    if (this.pendingRequests.has(key)) {
      return this.pendingRequests.get(key) as Promise<T>;
    }

    // Start new request
    const promise = fetcher().then(data => {
      this.cache.set(key, { data, timestamp: now, ttl });
      this.pendingRequests.delete(key);
      return data;
    }).catch(error => {
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

export interface DashboardParams {
  hoursBack?: number;
  siteId?: number;
}

export interface SiteParams {
  page?: number;
  size?: number;
  sortBy?: 'name' | 'location' | 'capacity';
  sortDir?: 'asc' | 'desc';
  search?: string;
}

export interface EnergyConsumptionParams {
  startDate?: string;
  endDate?: string;
  siteId?: number;
  aggregation?: 'HOURLY' | 'DAILY' | 'WEEKLY' | 'MONTHLY';
}

export interface CarbonFootprintParams {
  startDate?: string;
  endDate?: string;
  siteId?: number;
}

/**
 * Analytics API service functions
 */
export const analyticsAPI = {
  /**
   * Get dashboard analytics data
   */
  getDashboard: async (params: DashboardParams = {}): Promise<DashboardResponse> => {
    const cacheKey = `dashboard-${JSON.stringify(params)}`;
    
    return await requestCache.get(
      cacheKey,
      async () => {
        const searchParams = new URLSearchParams();
        
        if (params.hoursBack !== undefined) {
          searchParams.append('hoursBack', params.hoursBack.toString());
        }
        if (params.siteId !== undefined) {
          searchParams.append('siteId', params.siteId.toString());
        }

        const url = `/analytics/dashboard${searchParams.toString() ? `?${searchParams.toString()}` : ''}`;
        return await apiRequest(() => apiGateway.get<DashboardResponse>(url));
      },
      30000 // 30 second cache
    );
  },

  /**
   * Get energy consumption analytics
   */
  getEnergyConsumption: async (params: EnergyConsumptionParams = {}): Promise<EnergyConsumptionResponse> => {
    const cacheKey = `energy-consumption-${JSON.stringify(params)}`;
    
    return await requestCache.get(
      cacheKey,
      async () => {
        const searchParams = new URLSearchParams();
        const normalizeDate = (d?: string) => {
          if (!d) return undefined;
          // Backend expects LocalDateTime-like string without timezone (no trailing Z)
          // Accept a Date ISO string or already-local string. Strip trailing Z or offset.
          // Examples: '2025-10-04T02:59:04.987Z' -> '2025-10-04T02:59:04.987'
          try {
            // If it ends with Z, just remove it
            if (d.endsWith('Z')) return d.replace(/Z$/, '');
            // If it contains a timezone offset like +00:00, remove the offset
            const idx = d.search(/[+-]\d{2}:?\d{2}$/);
            if (idx !== -1) return d.substring(0, idx);
            return d;
          } catch (_) {
            return d;
          }
        };

        const s = normalizeDate(params.startDate);
        const e = normalizeDate(params.endDate);
        if (s) searchParams.append('startDate', s);
        if (e) searchParams.append('endDate', e);
        if (params.siteId !== undefined) {
          searchParams.append('siteId', params.siteId.toString());
        }
        if (params.aggregation) {
          searchParams.append('aggregation', params.aggregation);
        }

        const url = `/analytics/energy/consumption${searchParams.toString() ? `?${searchParams.toString()}` : ''}`;
        return await apiRequest(() => apiGateway.get<EnergyConsumptionResponse>(url));
      },
      60000 // 1 minute cache for time series data
    );
  },

  /**
   * Get carbon footprint analytics
   */
  getCarbonFootprint: async (params: CarbonFootprintParams = {}): Promise<CarbonFootprintResponse> => {
    const searchParams = new URLSearchParams();
    
    if (params.startDate) {
      searchParams.append('startDate', params.startDate);
    }
    if (params.endDate) {
      searchParams.append('endDate', params.endDate);
    }
    if (params.siteId !== undefined) {
      searchParams.append('siteId', params.siteId.toString());
    }

    const url = `/analytics/carbon/footprint${searchParams.toString() ? `?${searchParams.toString()}` : ''}`;
    return await apiRequest(() => apiGateway.get<CarbonFootprintResponse>(url));
  },

  /**
   * Get financial metrics
   */
  getFinancialMetrics: async (params: CarbonFootprintParams = {}): Promise<FinancialMetricsResponse> => {
    const searchParams = new URLSearchParams();
    
    if (params.startDate) {
      searchParams.append('startDate', params.startDate);
    }
    if (params.endDate) {
      searchParams.append('endDate', params.endDate);
    }
    if (params.siteId !== undefined) {
      searchParams.append('siteId', params.siteId.toString());
    }

    const url = `/analytics/financial/metrics${searchParams.toString() ? `?${searchParams.toString()}` : ''}`;
    return await apiRequest(() => apiGateway.get<FinancialMetricsResponse>(url));
  }
};

/**
 * Device API service functions
 */
export const deviceAPI = {
  /**
   * Get all devices
   */
  getDevices: async (siteId?: number): Promise<Device[]> => {
    const url = siteId ? `/devices?siteId=${siteId}` : '/devices';
    return await apiRequest(() => apiGateway.get<Device[]>(url));
  },

  /**
   * Get all sites
   */
  getSites: async (params: SiteParams = {}): Promise<Site[]> => {
    const cacheKey = 'sites';
    
    return await requestCache.get(
      cacheKey,
      async () => {
        const res = await apiRequest(() => apiGateway.get('/sites', { params }));
        // Gateway may return a paginated response { content: Site[], totalElements, ... }
        if (Array.isArray(res)) {
          return res as Site[];
        }
        if (res && Array.isArray(res.content)) {
          return res.content as Site[];
        }
        // Fallback: return empty array to avoid runtime errors in components
        return [] as Site[];
      },
      300000 // 5 minute cache for sites (they don't change often)
    );
  }
};

// Export cache management functions
export const cacheManager = {
  clear: () => requestCache.clear(),
  invalidate: (pattern?: string) => requestCache.invalidate(pattern),
  invalidateDashboard: () => requestCache.invalidate('dashboard'),
  invalidateEnergy: () => requestCache.invalidate('energy'),
  invalidateSites: () => requestCache.invalidate('sites')
};

export default analyticsAPI;