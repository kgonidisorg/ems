import { TokenManager } from './api';
import type { WebSocketMessage } from './types';

export type WebSocketEventType = 'connect' | 'disconnect' | 'message' | 'error' | 'reconnect';
export type WebSocketListener = (data?: unknown) => void;

/**
 * WebSocket client for real-time dashboard updates
 */
export class EcoGridWebSocket {
  private ws: WebSocket | null = null;
  private url: string;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 5000; // 5 seconds
  private heartbeatInterval: NodeJS.Timeout | null = null;
  private reconnectTimeout: NodeJS.Timeout | null = null;
  private listeners: Map<WebSocketEventType, WebSocketListener[]> = new Map();
  private isManuallyDisconnected = false;

  constructor(url: string = 'ws://localhost:8080/ws') {
    this.url = url;
    
    // Initialize event listener arrays
    this.listeners.set('connect', []);
    this.listeners.set('disconnect', []);
    this.listeners.set('message', []);
    this.listeners.set('error', []);
    this.listeners.set('reconnect', []);
  }

  /**
   * Connect to WebSocket server
   */
  connect(): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('WebSocket is already connected');
      return;
    }

    try {
      const token = TokenManager.getToken();
      const wsUrl = token ? `${this.url}?token=${encodeURIComponent(token)}` : this.url;
      
      this.ws = new WebSocket(wsUrl);
      this.isManuallyDisconnected = false;

      this.ws.onopen = (event) => {
        console.log('WebSocket connected');
        this.reconnectAttempts = 0;
        this.startHeartbeat();
        this.emit('connect', event);
      };

      this.ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          console.log('WebSocket message received:', message);
          this.emit('message', message);
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error);
        }
      };

      this.ws.onclose = (event) => {
        console.log('WebSocket disconnected:', event.code, event.reason);
        this.stopHeartbeat();
        this.emit('disconnect', event);
        
        if (!this.isManuallyDisconnected && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.scheduleReconnect();
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        this.emit('error', error);
      };

    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      this.emit('error', error);
    }
  }

  /**
   * Disconnect from WebSocket server
   */
  disconnect(): void {
    this.isManuallyDisconnected = true;
    this.stopHeartbeat();
    
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
      this.reconnectTimeout = null;
    }

    if (this.ws) {
      this.ws.close(1000, 'Manual disconnect');
      this.ws = null;
    }
  }

  /**
   * Send message to server
   */
  send(message: Record<string, unknown>): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    } else {
      console.warn('WebSocket is not connected. Cannot send message:', message);
    }
  }

  /**
   * Add event listener
   */
  on(event: WebSocketEventType, listener: WebSocketListener): void {
    const eventListeners = this.listeners.get(event);
    if (eventListeners) {
      eventListeners.push(listener);
    }
  }

  /**
   * Remove event listener
   */
  off(event: WebSocketEventType, listener: WebSocketListener): void {
    const eventListeners = this.listeners.get(event);
    if (eventListeners) {
      const index = eventListeners.indexOf(listener);
      if (index > -1) {
        eventListeners.splice(index, 1);
      }
    }
  }

  /**
   * Remove all listeners for an event
   */
  removeAllListeners(event?: WebSocketEventType): void {
    if (event) {
      this.listeners.set(event, []);
    } else {
      this.listeners.clear();
      this.listeners.set('connect', []);
      this.listeners.set('disconnect', []);
      this.listeners.set('message', []);
      this.listeners.set('error', []);
      this.listeners.set('reconnect', []);
    }
  }

  /**
   * Get connection state
   */
  getState(): string {
    if (!this.ws) return 'CLOSED';
    
    switch (this.ws.readyState) {
      case WebSocket.CONNECTING:
        return 'CONNECTING';
      case WebSocket.OPEN:
        return 'OPEN';
      case WebSocket.CLOSING:
        return 'CLOSING';
      case WebSocket.CLOSED:
        return 'CLOSED';
      default:
        return 'UNKNOWN';
    }
  }

  /**
   * Check if connected
   */
  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }

  private emit(event: WebSocketEventType, data?: unknown): void {
    const eventListeners = this.listeners.get(event);
    if (eventListeners) {
      eventListeners.forEach(listener => listener(data));
    }
  }

  private startHeartbeat(): void {
    this.stopHeartbeat();
    this.heartbeatInterval = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send({ type: 'ping', timestamp: new Date().toISOString() });
      }
    }, 30000); // Send ping every 30 seconds
  }

  private stopHeartbeat(): void {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }
  }

  private scheduleReconnect(): void {
    this.reconnectAttempts++;
    const delay = this.reconnectInterval * Math.pow(2, Math.min(this.reconnectAttempts - 1, 3)); // Exponential backoff

    console.log(`Scheduling reconnect attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts} in ${delay}ms`);
    
    this.reconnectTimeout = setTimeout(() => {
      console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
      this.emit('reconnect', { attempt: this.reconnectAttempts });
      this.connect();
    }, delay);
  }
}

// Singleton instance
let wsInstance: EcoGridWebSocket | null = null;

/**
 * Get WebSocket singleton instance
 */
export const getWebSocketInstance = (): EcoGridWebSocket => {
  if (!wsInstance) {
    wsInstance = new EcoGridWebSocket();
  }
  return wsInstance;
};

/**
 * React hook for WebSocket connection
 */
export const useWebSocket = () => {
  const ws = getWebSocketInstance();
  
  return {
    connect: () => ws.connect(),
    disconnect: () => ws.disconnect(),
    send: (message: Record<string, unknown>) => ws.send(message),
    on: (event: WebSocketEventType, listener: WebSocketListener) => ws.on(event, listener),
    off: (event: WebSocketEventType, listener: WebSocketListener) => ws.off(event, listener),
    isConnected: () => ws.isConnected(),
    getState: () => ws.getState(),
  };
};

export default EcoGridWebSocket;