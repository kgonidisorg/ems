-- EcoGrid EMS Database Initialization Script
-- Creates separate databases for each microservice
-- Create databases for each service
CREATE DATABASE ems_auth;

CREATE DATABASE ems_devices;

CREATE DATABASE ems_analytics;

CREATE DATABASE ems_notifications;

-- Grant permissions to the ems_user
GRANT ALL PRIVILEGES ON DATABASE ems_auth TO ems_user;

GRANT ALL PRIVILEGES ON DATABASE ems_devices TO ems_user;

GRANT ALL PRIVILEGES ON DATABASE ems_analytics TO ems_user;

GRANT ALL PRIVILEGES ON DATABASE ems_notifications TO ems_user;

-- Connect to each database and create extensions if needed
\c ems_auth;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c ems_devices;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c ems_analytics;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c ems_notifications;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enhanced EMS Device Schema Setup
\c ems_devices;
-- Create device_types table
CREATE TABLE IF NOT EXISTS device_types(
  id serial PRIMARY KEY,
  name varchar(100) NOT NULL UNIQUE,
  category varchar(50) NOT NULL,
  telemetry_schema jsonb,
  alert_thresholds jsonb,
  specifications jsonb,
  created_at timestamp with time zone DEFAULT NOW()
);

-- Create device_telemetry table
CREATE TABLE IF NOT EXISTS device_telemetry(
  id serial PRIMARY KEY,
  device_id bigint NOT NULL,
  timestamp timestamp with time zone NOT NULL,
  data jsonb NOT NULL,
  quality_indicators jsonb,
  processed_at timestamp with time zone,
  created_at timestamp with time zone DEFAULT NOW()
);

-- Create device_status_cache table
CREATE TABLE IF NOT EXISTS device_status_cache(
  device_id bigint PRIMARY KEY,
  last_seen timestamp with time zone,
  status varchar(20) NOT NULL CHECK (status IN ('ONLINE', 'OFFLINE', 'FAULT', 'MAINTENANCE')),
  current_data jsonb,
  alert_count integer DEFAULT 0,
  uptime_24h DECIMAL(5, 2),
  updated_at timestamp with time zone DEFAULT NOW()
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_device_type_name ON device_types(name);

CREATE INDEX IF NOT EXISTS idx_device_type_category ON device_types(category);

CREATE INDEX IF NOT EXISTS idx_telemetry_device_timestamp ON device_telemetry(device_id, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_telemetry_timestamp ON device_telemetry(timestamp DESC);

-- Insert default device types
INSERT INTO device_types(name, category, telemetry_schema, alert_thresholds, specifications)
  VALUES ('BMS', 'STORAGE', '{"soc": "number", "remainingCapacity": "number", "nominalCapacity": "number", "chargeRate": "number", "voltage": "number", "current": "number", "temperature": "number", "healthStatus": "string", "efficiency": "number"}'::jsonb, '{"HIGH_TEMPERATURE": {"threshold": 45, "severity": "HIGH"}, "LOW_SOC": {"threshold": 20, "severity": "MEDIUM"}, "HIGH_SOC": {"threshold": 95, "severity": "LOW"}}'::jsonb, '{"maxCapacity": "1000kWh", "maxPower": "500kW", "chemistry": "LiFePO4"}'::jsonb),
('SOLAR_ARRAY', 'GENERATION', '{"currentOutput": "number", "energyYield": "number", "panelTemperature": "number", "irradiance": "number", "inverterEfficiency": "number", "systemEfficiency": "number", "performanceRatio": "number", "inverterStatus": "string"}'::jsonb, '{"HIGH_PANEL_TEMPERATURE": {"threshold": 80, "severity": "MEDIUM"}, "INVERTER_FAULT": {"severity": "HIGH"}, "PERFORMANCE_DEGRADATION": {"threshold": 0.8, "severity": "MEDIUM"}}'::jsonb, '{"maxPower": "250kW", "panelType": "Monocrystalline", "inverterType": "String"}'::jsonb),
('EV_CHARGER', 'CHARGING', '{"activeSessions": "number", "totalSessions": "number", "powerDelivered": "number", "energyDelivered": "number", "revenue": "number", "avgSessionDuration": "number", "utilizationRate": "number", "networkConnectivity": "boolean", "paymentSystemStatus": "string", "faults": "number", "uptime": "number"}'::jsonb, '{"CHARGER_OFFLINE": {"severity": "HIGH"}, "PAYMENT_SYSTEM_FAULT": {"severity": "MEDIUM"}, "LOW_UTILIZATION": {"threshold": 0.3, "severity": "LOW"}}'::jsonb, '{"maxPower": "150kW", "connectorTypes": ["CCS", "CHAdeMO"], "chargersPerStation": 4}'::jsonb)
ON CONFLICT (name)
  DO NOTHING;

