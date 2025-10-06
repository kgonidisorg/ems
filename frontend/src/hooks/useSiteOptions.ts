import { useState, useEffect } from 'react';
import { SiteOption } from '@/lib/types';
import { SiteService } from '@/lib/api';

interface UseSiteOptionsReturn {
  sites: SiteOption[];
  loading: boolean;
  error: string | null;
}

/**
 * Custom hook for fetching site options for dropdown selection
 * 
 * @returns Object containing sites array, loading state, and error state
 */
export function useSiteOptions(): UseSiteOptionsReturn {
  const [sites, setSites] = useState<SiteOption[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchSites = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const siteOptions = await SiteService.getSiteOptions();
        setSites(siteOptions);
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to fetch sites';
        setError(errorMessage);
        console.error('Error fetching site options:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchSites();
  }, []);

  return {
    sites,
    loading,
    error,
  };
}