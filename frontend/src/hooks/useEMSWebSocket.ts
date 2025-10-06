import React from 'react';
import { 
  EMSWebSocketDelta, 
  SiteInfoData, 
  BatterySystemData, 
  SolarArrayData, 
  EVChargerData, 
  OperationalData,
  ForecastData,
  ScheduleData
} from '@/types/websocket';

export interface EMSData {
  siteInfo: SiteInfoData | null;
  batterySystem: BatterySystemData | null;
  solarArray: SolarArrayData | null;
  evCharger: EVChargerData | null;
  operationalData: OperationalData | null;
  forecast: ForecastData[];
  schedule: ScheduleData[];
  lastUpdated: Date | null;
  connectionStatus: 'connecting' | 'connected' | 'disconnected' | 'error';
}

export interface WebSocketCallbacks {
  onData?: (data: EMSData) => void;
  onConnectionChange?: (status: EMSData['connectionStatus']) => void;
  onError?: (error: Error) => void;
}

export class EMSWebSocketClient {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 3000; // 3 seconds
  private reconnectTimer: NodeJS.Timeout | null = null;
  private siteId: string;
  private callbacks: WebSocketCallbacks;
  
  // Current state - accumulates data from delta updates
  private currentData: EMSData = {
    siteInfo: null,
    batterySystem: null,
    solarArray: null,
    evCharger: null,
    operationalData: null,
    forecast: [],
    schedule: [],
    lastUpdated: null,
    connectionStatus: 'disconnected'
  };

  constructor(siteId: string, callbacks: WebSocketCallbacks = {}) {
    this.siteId = siteId;
    this.callbacks = callbacks;
  }

  /**
   * Connect to WebSocket server
   */
  connect(): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('WebSocket already connected');
      return;
    }

    try {
      // WebSocket URL - adjust based on your backend configuration
      const wsUrl = `ws://localhost:8080/ws`;
      
      this.ws = new WebSocket(wsUrl);
      this.updateConnectionStatus('connecting');

      this.ws.onopen = this.handleOpen.bind(this);
      this.ws.onmessage = this.handleMessage.bind(this);
      this.ws.onclose = this.handleClose.bind(this);
      this.ws.onerror = this.handleError.bind(this);

    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      this.updateConnectionStatus('error');
      if (this.callbacks.onError) {
        this.callbacks.onError(error as Error);
      }
    }
  }

  /**
   * Disconnect from WebSocket server
   */
  disconnect(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }

    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }

    this.updateConnectionStatus('disconnected');
  }

  /**
   * Get current data state
   */
  getCurrentData(): EMSData {
    return { ...this.currentData };
  }

  private handleOpen(): void {
    console.log('WebSocket connected for site:', this.siteId);
    this.reconnectAttempts = 0;
    this.updateConnectionStatus('connected');

    // Subscribe to site-specific updates
    this.subscribeToSite(this.siteId);
  }

  private handleMessage(event: MessageEvent): void {
    try {
      const delta: EMSWebSocketDelta = JSON.parse(event.data);
      
      if (delta.siteId !== this.siteId) {
        // Ignore messages for other sites
        return;
      }

      console.log('Received WebSocket delta:', delta.type, delta);
      
      // Apply delta update to current state
      this.applyDelta(delta);
      
      // Notify callbacks
      if (this.callbacks.onData) {
        this.callbacks.onData(this.getCurrentData());
      }

    } catch (error) {
      console.error('Error parsing WebSocket message:', error);
      if (this.callbacks.onError) {
        this.callbacks.onError(error as Error);
      }
    }
  }

  private handleClose(event: CloseEvent): void {
    console.log('WebSocket connection closed:', event.code, event.reason);
    this.updateConnectionStatus('disconnected');

    // Attempt to reconnect if not a normal closure
    if (event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
      this.scheduleReconnect();
    }
  }

  private handleError(event: Event): void {
    console.error('WebSocket error:', event);
    this.updateConnectionStatus('error');
    
    if (this.callbacks.onError) {
      this.callbacks.onError(new Error('WebSocket connection error'));
    }
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }

    this.reconnectAttempts++;
    const delay = this.reconnectInterval * Math.pow(2, this.reconnectAttempts - 1); // Exponential backoff
    
    console.log(`Scheduling reconnect attempt ${this.reconnectAttempts} in ${delay}ms`);
    
    this.reconnectTimer = setTimeout(() => {
      this.connect();
    }, delay);
  }

  private subscribeToSite(siteId: string): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      // Send subscription message to backend
      const subscriptionMessage = {
        action: 'SUBSCRIBE',
        destination: `/topic/sites/${siteId}/dashboard`,
        siteId: siteId
      };
      
      this.ws.send(JSON.stringify(subscriptionMessage));
      console.log('Subscribed to site updates:', siteId);
    }
  }

  private applyDelta(delta: EMSWebSocketDelta): void {
    // Update last updated timestamp
    this.currentData.lastUpdated = new Date();

    switch (delta.type) {
      case 'FULL_UPDATE':
        // Replace entire sections with new data
        this.applyFullUpdate(delta);
        break;
        
      case 'DELTA_UPDATE':
        // Merge changed fields only
        this.applyDeltaUpdate(delta);
        break;
        
      case 'ALERT_UPDATE':
      case 'DEVICE_STATUS_UPDATE':
        // Handle specific update types
        this.applyDeltaUpdate(delta);
        break;
    }
  }

  private applyFullUpdate(delta: EMSWebSocketDelta): void {
    if (delta.siteInfo) {
      this.currentData.siteInfo = { ...delta.siteInfo };
    }
    if (delta.batterySystem) {
      this.currentData.batterySystem = { ...delta.batterySystem };
    }
    if (delta.solarArray) {
      this.currentData.solarArray = { ...delta.solarArray };
    }
    if (delta.evCharger) {
      this.currentData.evCharger = { ...delta.evCharger };
    }
    if (delta.operationalData) {
      this.currentData.operationalData = { ...delta.operationalData };
    }
    if (delta.forecast) {
      this.currentData.forecast = [...delta.forecast];
    }
    if (delta.schedule) {
      this.currentData.schedule = [...delta.schedule];
    }
  }

  private applyDeltaUpdate(delta: EMSWebSocketDelta): void {
    // Apply selective updates for each section
    if (delta.siteInfo && this.currentData.siteInfo) {
      this.currentData.siteInfo = { ...this.currentData.siteInfo, ...delta.siteInfo };
    } else if (delta.siteInfo) {
      this.currentData.siteInfo = { ...delta.siteInfo };
    }

    if (delta.batterySystem && this.currentData.batterySystem) {
      this.currentData.batterySystem = { ...this.currentData.batterySystem, ...delta.batterySystem };
    } else if (delta.batterySystem) {
      this.currentData.batterySystem = { ...delta.batterySystem };
    }

    if (delta.solarArray && this.currentData.solarArray) {
      this.currentData.solarArray = { ...this.currentData.solarArray, ...delta.solarArray };
    } else if (delta.solarArray) {
      this.currentData.solarArray = { ...delta.solarArray };
    }

    if (delta.evCharger && this.currentData.evCharger) {
      this.currentData.evCharger = { ...this.currentData.evCharger, ...delta.evCharger };
    } else if (delta.evCharger) {
      this.currentData.evCharger = { ...delta.evCharger };
    }

    if (delta.operationalData && this.currentData.operationalData) {
      this.currentData.operationalData = { ...this.currentData.operationalData, ...delta.operationalData };
    } else if (delta.operationalData) {
      this.currentData.operationalData = { ...delta.operationalData };
    }

    // Handle forecast and schedule arrays with change types
    if (delta.forecast) {
      this.currentData.forecast = [...delta.forecast];
    }
    if (delta.schedule) {
      this.currentData.schedule = [...delta.schedule];
    }
  }



  private updateConnectionStatus(status: EMSData['connectionStatus']): void {
    this.currentData.connectionStatus = status;
    if (this.callbacks.onConnectionChange) {
      this.callbacks.onConnectionChange(status);
    }
  }
}

// Hook for React components
export const useEMSWebSocket = (siteId: string) => {
  const [data, setData] = React.useState<EMSData>({
    siteInfo: null,
    batterySystem: null,
    solarArray: null,
    evCharger: null,
    operationalData: null,
    forecast: [],
    schedule: [],
    lastUpdated: null,
    connectionStatus: 'disconnected'
  });

  const [client, setClient] = React.useState<EMSWebSocketClient | null>(null);

  React.useEffect(() => {
    const wsClient = new EMSWebSocketClient(siteId, {
      onData: (newData) => {
        setData(newData);
      },
      onConnectionChange: (status) => {
        setData(prev => ({ ...prev, connectionStatus: status }));
      },
      onError: (error) => {
        console.error('WebSocket error:', error);
      }
    });

    setClient(wsClient);
    wsClient.connect();

    return () => {
      wsClient.disconnect();
    };
  }, [siteId]);

  return {
    data,
    client,
    isConnected: data.connectionStatus === 'connected',
    isConnecting: data.connectionStatus === 'connecting',
    hasError: data.connectionStatus === 'error'
  };
};