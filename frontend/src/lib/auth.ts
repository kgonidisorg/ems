import { apiGateway, apiRequest, TokenManager } from './api';
import { LoginRequest, LoginResponse, RegisterRequest, User } from './types';

export class AuthService {
  /**
   * Login user with username and password
   */
  static async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiRequest<LoginResponse>(() =>
      apiGateway.post('/auth/login', credentials)
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
      apiGateway.post('/auth/register', userData)
    );
  }

  /**
   * Logout user
   */
  static async logout(): Promise<void> {
    try {
      // Call logout endpoint to invalidate server-side session
      // This is optional - if it fails, we still proceed with local cleanup
      await apiRequest<void>(() =>
        apiGateway.post('/auth/logout')
      );
    } catch (error) {
      // Log the error in development, but don't show it to users
      // The logout API call failing is not critical since we clear local storage anyway
      if (process.env.NODE_ENV === 'development') {
        console.warn('Logout API call failed (non-critical):', error);
      }
      // In production, we might want to send this to an error tracking service
      // but not show it to the user since local logout still works
    } finally {
      // Always clear local storage - this is the critical part for security
      TokenManager.removeToken();
      TokenManager.removeUser();
      
      // Trigger a custom event to notify other parts of the app
      window.dispatchEvent(new CustomEvent('auth:logout'));
    }
  }

  /**
   * Get current user profile
   */
  static async getCurrentUser(): Promise<User> {
    return await apiRequest<User>(() =>
      apiGateway.get('/auth/me')
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
      apiGateway.post('/auth/forgot-password', { email })
    );
  }

  /**
   * Reset password with token
   */
  static async resetPassword(token: string, newPassword: string): Promise<void> {
    await apiRequest<void>(() =>
      apiGateway.post('/auth/reset-password', { token, newPassword })
    );
  }

  /**
   * Change password for authenticated user
   */
  static async changePassword(currentPassword: string, newPassword: string): Promise<void> {
    await apiRequest<void>(() =>
      apiGateway.post('/auth/change-password', { currentPassword, newPassword })
    );
  }
}