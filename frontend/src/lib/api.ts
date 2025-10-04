import axios, { AxiosInstance, AxiosResponse } from 'axios';
import Cookies from 'js-cookie';
import { User } from './types';

// Configuration for different services
const API_CONFIG = {
  API_GATEWAY: 'http://localhost:8080/api',
  AUTH_SERVICE: 'http://localhost:8081',
  DEVICE_SERVICE: 'http://localhost:8082',
  ANALYTICS_SERVICE: 'http://localhost:8083',
  NOTIFICATION_SERVICE: 'http://localhost:8084',
};

// Token management
export const TokenManager = {
  getToken: (): string | null => {
    return Cookies.get('jwt_token') || localStorage.getItem('jwt_token');
  },
  
  setToken: (token: string): void => {
    Cookies.set('jwt_token', token, { expires: 7, secure: true, sameSite: 'strict' });
    localStorage.setItem('jwt_token', token);
  },
  
  removeToken: (): void => {
    Cookies.remove('jwt_token');
    localStorage.removeItem('jwt_token');
  },
  
  getUser: (): User | null => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },
  
  setUser: (user: User): void => {
    localStorage.setItem('user', JSON.stringify(user));
  },
  
  removeUser: (): void => {
    localStorage.removeItem('user');
  }
};

// Create axios instances for different services
const createApiInstance = (baseURL: string): AxiosInstance => {
  const instance = axios.create({
    baseURL,
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
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
        // Redirect to login page
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );

  return instance;
};

// API instances
export const apiGateway = createApiInstance(API_CONFIG.API_GATEWAY);
export const authService = createApiInstance(API_CONFIG.AUTH_SERVICE);
export const deviceService = createApiInstance(API_CONFIG.DEVICE_SERVICE);
export const analyticsService = createApiInstance(API_CONFIG.ANALYTICS_SERVICE);
export const notificationService = createApiInstance(API_CONFIG.NOTIFICATION_SERVICE);

// Re-export types from types.ts
export type { ApiResponse, ApiError } from './types';

// Generic API request wrapper
export const apiRequest = async <T>(
  request: () => Promise<AxiosResponse<T>>
): Promise<T> => {
  try {
    const response = await request();
    return response.data;
  } catch (error: unknown) {
    const axiosError = error as { response?: { data?: { message?: string; code?: string } }; message?: string; code?: string };
    const apiError = {
      message: axiosError.response?.data?.message || axiosError.message || 'An error occurred',
      code: axiosError.response?.data?.code || axiosError.code,
      details: axiosError.response?.data
    };
    throw apiError;
  }
};