import { useState, useEffect, useCallback, useRef } from 'react';

export interface UseAsyncDataOptions {
  autoLoad?: boolean;
  dependencies?: unknown[];
  retryAttempts?: number;
  retryDelay?: number;
}

export interface UseAsyncDataResult<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  setData: (data: T | null) => void;
}

/**
 * Custom hook for managing async data fetching with loading and error states
 */
export function useAsyncData<T>(
  fetchFunction: () => Promise<T>,
  options: UseAsyncDataOptions = {}
): UseAsyncDataResult<T> {
  const {
    autoLoad = true,
    dependencies = [],
    retryAttempts = 3,
    retryDelay = 1000
  } = options;

  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [retryCount, setRetryCount] = useState(0);
  
  const fetchRef = useRef(fetchFunction);
  fetchRef.current = fetchFunction;

  const fetchData = useCallback(async (isRetry = false) => {
    if (!isRetry) {
      setLoading(true);
      setError(null);
      setRetryCount(0);
    }

    try {
      const result = await fetchRef.current();
      setData(result);
      setError(null);
      setRetryCount(0);
    } catch (err) {
      console.error('Data fetch error:', err);
      const errorMessage = err instanceof Error ? err.message : 'An error occurred while fetching data';
      
      if (retryCount < retryAttempts) {
        console.log(`Retrying... (${retryCount + 1}/${retryAttempts})`);
        setRetryCount(prev => prev + 1);
        setTimeout(() => {
          fetchData(true);
        }, retryDelay * Math.pow(2, retryCount)); // Exponential backoff
      } else {
        setError(errorMessage);
      }
    } finally {
      if (!isRetry) {
        setLoading(false);
      }
    }
  }, [retryAttempts, retryDelay, retryCount]);

  const refetch = useCallback(async () => {
    setRetryCount(0);
    await fetchData();
  }, [fetchData]);

  useEffect(() => {
    if (autoLoad) {
      fetchData();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [autoLoad, ...dependencies]);

  return {
    data,
    loading,
    error,
    refetch,
    setData
  };
}

/**
 * Custom hook for managing multiple async data sources
 */
export function useMultiAsyncData<T extends Record<string, unknown>>(
  fetchFunctions: Record<keyof T, () => Promise<T[keyof T]>>,
  options: UseAsyncDataOptions = {}
): {
  data: Partial<T>;
  loading: boolean;
  errors: Partial<Record<keyof T, string>>;
  refetch: (key?: keyof T) => Promise<void>;
} {
  const [data, setData] = useState<Partial<T>>({});
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Partial<Record<keyof T, string>>>({});

  const fetchData = useCallback(async (specificKey?: keyof T) => {
    setLoading(true);
    
    const keysToFetch = specificKey ? [specificKey] : Object.keys(fetchFunctions) as (keyof T)[];
    
    const promises = keysToFetch.map(async (key) => {
      try {
        const result = await fetchFunctions[key]();
        setData(prev => ({ ...prev, [key]: result }));
        setErrors(prev => ({ ...prev, [key]: undefined }));
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : `Error fetching ${String(key)}`;
        setErrors(prev => ({ ...prev, [key]: errorMessage }));
        console.error(`Error fetching ${String(key)}:`, err);
      }
    });

    await Promise.allSettled(promises);
    setLoading(false);
  }, [fetchFunctions]);

  const refetch = useCallback(async (key?: keyof T) => {
    await fetchData(key);
  }, [fetchData]);

  useEffect(() => {
    if (options.autoLoad !== false) {
      fetchData();
    }
  }, [fetchData, options.autoLoad, options.dependencies]);

  return {
    data,
    loading,
    errors,
    refetch
  };
}

export default useAsyncData;