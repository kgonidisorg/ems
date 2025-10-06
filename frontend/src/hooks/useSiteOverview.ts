import { useState, useEffect, useCallback } from 'react';
import { SiteOverview } from '@/lib/types';
import { SiteService } from '@/lib/api';

interface UseSiteOverviewOptions {
  siteId: number | null;
  refreshInterval?: number; // Optional auto-refresh interval in milliseconds
}

interface UseSiteOverviewReturn {
  data: SiteOverview | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

/**
 * Custom hook for fetching and managing site overview data
 * 
 * @param options - Configuration options including siteId and optional refresh interval
 * @returns Object containing data, loading state, error state, and refetch function
 */
export function useSiteOverview({ 
  siteId, 
  refreshInterval 
}: UseSiteOverviewOptions): UseSiteOverviewReturn {
  const [data, setData] = useState<SiteOverview | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchSiteOverview = useCallback(async () => {
    if (!siteId) {
      setData(null);
      setLoading(false);
      setError(null);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
      const overview = await SiteService.getSiteOverview(siteId);
      setData(overview);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
      setError(errorMessage);
      console.error('Error fetching site overview:', err);
    } finally {
      setLoading(false);
    }
  }, [siteId]);

  // Fetch data when siteId changes
  useEffect(() => {
    fetchSiteOverview();
  }, [fetchSiteOverview]);

  // Optional auto-refresh functionality
  useEffect(() => {
    if (!refreshInterval || !siteId) return;

    const interval = setInterval(() => {
      fetchSiteOverview();
    }, refreshInterval);

    return () => clearInterval(interval);
  }, [fetchSiteOverview, refreshInterval, siteId]);

  const refetch = useCallback(async () => {
    await fetchSiteOverview();
  }, [fetchSiteOverview]);

  return {
    data,
    loading,
    error,
    refetch,
  };
}