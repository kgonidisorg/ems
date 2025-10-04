import { authService, apiRequest, TokenManager } from './api';
import { LoginRequest, LoginResponse, RegisterRequest, User } from './types';

export class AuthService {
  /**
   * Login user with username and password
   */
  static async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiRequest<LoginResponse>(() =>
      authService.post('/auth/login', credentials)
    );
    
    // Store token and user data
    TokenManager.setToken(response.token);
    TokenManager.setUser(response.user);
    
    return response;
  }

  /**
   * Register new user
   */
  static async register(userData: RegisterRequest): Promise<User> {
    return await apiRequest<User>(() =>
      authService.post('/auth/register', userData)
    );
  }

  /**
   * Logout user
   */
  static async logout(): Promise<void> {
    try {
      // Call logout endpoint if available
      await apiRequest<void>(() =>
        authService.post('/auth/logout')
      );
    } catch (error) {
      // Even if the API call fails, we should clear local storage
      console.warn('Logout API call failed, but clearing local storage:', error);
    } finally {
      // Always clear local storage
      TokenManager.removeToken();
      TokenManager.removeUser();
    }
  }

  /**
   * Get current user profile
   */
  static async getCurrentUser(): Promise<User> {
    return await apiRequest<User>(() =>
      authService.get('/auth/me')
    );
  }

  /**
   * Check if user is authenticated
   */
  static isAuthenticated(): boolean {
    const token = TokenManager.getToken();
    const user = TokenManager.getUser();
    return !!(token && user);
  }

  /**
   * Get current user from storage
   */
  static getCurrentUserFromStorage(): User | null {
    return TokenManager.getUser();
  }

  /**
   * Refresh user data from server
   */
  static async refreshUser(): Promise<User> {
    const user = await this.getCurrentUser();
    TokenManager.setUser(user);
    return user;
  }

  /**
   * Request password reset
   */
  static async requestPasswordReset(email: string): Promise<void> {
    await apiRequest<void>(() =>
      authService.post('/auth/forgot-password', { email })
    );
  }

  /**
   * Reset password with token
   */
  static async resetPassword(token: string, newPassword: string): Promise<void> {
    await apiRequest<void>(() =>
      authService.post('/auth/reset-password', { token, newPassword })
    );
  }

  /**
   * Change password for authenticated user
   */
  static async changePassword(currentPassword: string, newPassword: string): Promise<void> {
    await apiRequest<void>(() =>
      authService.post('/auth/change-password', { currentPassword, newPassword })
    );
  }
}