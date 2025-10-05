-- Enhanced EMS Device Service Database Schema
-- Phase 5: Real-time EMS Integration
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

CREATE INDEX IF NOT EXISTS idx_device_type_name ON device_types(name);

CREATE INDEX IF NOT EXISTS idx_device_type_category ON device_types(category);

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

CREATE INDEX IF NOT EXISTS idx_telemetry_device_timestamp ON device_telemetry(device_id, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_telemetry_timestamp ON device_telemetry(timestamp DESC);

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

-- Insert default device types for EMS
INSERT INTO device_types(name, category, telemetry_schema, alert_thresholds, specifications)
  VALUES ('BMS', 'STORAGE', '{"soc": "number", "remainingCapacity": "number", "nominalCapacity": "number", "chargeRate": "number", "voltage": "number", "current": "number", "temperature": "number", "healthStatus": "string", "efficiency": "number"}'::jsonb, '{"HIGH_TEMPERATURE": {"threshold": 45, "severity": "HIGH"}, "LOW_SOC": {"threshold": 20, "severity": "MEDIUM"}, "HIGH_SOC": {"threshold": 95, "severity": "LOW"}}'::jsonb, '{"maxCapacity": "1000kWh", "maxPower": "500kW", "chemistry": "LiFePO4"}'::jsonb),
('SOLAR_ARRAY', 'GENERATION', '{"currentOutput": "number", "energyYield": "number", "panelTemperature": "number", "irradiance": "number", "inverterEfficiency": "number", "systemEfficiency": "number", "performanceRatio": "number", "inverterStatus": "string"}'::jsonb, '{"HIGH_PANEL_TEMPERATURE": {"threshold": 80, "severity": "MEDIUM"}, "INVERTER_FAULT": {"severity": "HIGH"}, "PERFORMANCE_DEGRADATION": {"threshold": 0.8, "severity": "MEDIUM"}}'::jsonb, '{"maxPower": "250kW", "panelType": "Monocrystalline", "inverterType": "String"}'::jsonb),
('EV_CHARGER', 'CHARGING', '{"activeSessions": "number", "totalSessions": "number", "powerDelivered": "number", "energyDelivered": "number", "revenue": "number", "avgSessionDuration": "number", "utilizationRate": "number", "networkConnectivity": "boolean", "paymentSystemStatus": "string", "faults": "number", "uptime": "number"}'::jsonb, '{"CHARGER_OFFLINE": {"severity": "HIGH"}, "PAYMENT_SYSTEM_FAULT": {"severity": "MEDIUM"}, "LOW_UTILIZATION": {"threshold": 0.3, "severity": "LOW"}}'::jsonb, '{"maxPower": "150kW", "connectorTypes": ["CCS", "CHAdeMO"], "chargersPerStation": 4}'::jsonb)
ON CONFLICT (name)
  DO NOTHING;

-- Update existing devices table to use device_type_id (if it doesn't already have this column)
DO $$
BEGIN
  IF NOT EXISTS(
    SELECT
      1
    FROM
      information_schema.columns
    WHERE
      table_name = 'devices'
      AND column_name = 'device_type_id') THEN
  ALTER TABLE devices
    ADD COLUMN device_type_id bigint;
  ALTER TABLE devices
    ADD CONSTRAINT fk_device_device_type FOREIGN KEY(device_type_id) REFERENCES device_types(id);
  CREATE INDEX idx_device_device_type ON devices(device_type_id);
  -- Update existing devices to reference device types
  UPDATE
    devices
  SET
    device_type_id =(
      CASE WHEN device_type = 'BATTERY_STORAGE' THEN
(
        SELECT
          id
        FROM
          device_types
        WHERE
          name = 'BMS')
      WHEN device_type = 'SOLAR_INVERTER' THEN
(
        SELECT
          id
        FROM
          device_types
        WHERE
          name = 'SOLAR_ARRAY')
      WHEN device_type = 'EV_CHARGER' THEN
(
        SELECT
          id
        FROM
          device_types
        WHERE
          name = 'EV_CHARGER')
      ELSE
(
          SELECT
            id
          FROM
            device_types
          WHERE
            name = 'BMS') -- Default
      END);
END IF;
END
$$;

-- Add foreign key constraints for device_telemetry and device_status_cache
ALTER TABLE device_telemetry
  ADD CONSTRAINT IF NOT EXISTS fk_telemetry_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE;

ALTER TABLE device_status_cache
  ADD CONSTRAINT IF NOT EXISTS fk_status_cache_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE;

-- Create sample test data for development
DO $$
DECLARE
  site_id bigint;
  bms_type_id bigint;
  solar_type_id bigint;
  ev_type_id bigint;
  device_id bigint;
BEGIN
  -- Get device type IDs
  SELECT
    id INTO bms_type_id
  FROM
    device_types
  WHERE
    name = 'BMS';
  SELECT
    id INTO solar_type_id
  FROM
    device_types
  WHERE
    name = 'SOLAR_ARRAY';
  SELECT
    id INTO ev_type_id
  FROM
    device_types
  WHERE
    name = 'EV_CHARGER';
  -- Create sample site if it doesn't exist
  INSERT INTO sites(name, description, location_lat, location_lng, capacity_mw, status, contact_person, contact_email, contact_phone, address)
    VALUES ('New York Energy Hub', 'Primary energy management site in New York', 40.7128, -74.0060, 10.5, 'ACTIVE', 'John Smith', 'contact@ecogrid.com', '+1 (555) 123-4567', 'New York, NY, USA')
  ON CONFLICT (name)
    DO NOTHING
  RETURNING
    id INTO site_id;
  -- Get site ID if it already exists
  IF site_id IS NULL THEN
    SELECT
      id INTO site_id
    FROM
      sites
    WHERE
      name = 'New York Energy Hub';
  END IF;
  -- Create sample BMS device
  INSERT INTO devices(serial_number, name, description, device_type_id, model, manufacturer, status, rated_power_kw, mqtt_topic, site_id)
    VALUES ('BMS001-NY', 'Battery Management System 1', 'Primary BMS for energy storage', bms_type_id, 'Tesla Megapack 2XL', 'Tesla', 'ONLINE', 500.0, 'ecogrid/sites/1/devices/1/telemetry', site_id)
  ON CONFLICT (serial_number)
    DO NOTHING
  RETURNING
    id INTO device_id;
  -- Create sample Solar device
  INSERT INTO devices(serial_number, name, description, device_type_id, model, manufacturer, status, rated_power_kw, mqtt_topic, site_id)
    VALUES ('SOLAR001-NY', 'Solar Array 1', 'Primary solar generation array', solar_type_id, 'SolarEdge SE100K', 'SolarEdge', 'ONLINE', 250.0, 'ecogrid/sites/1/devices/2/telemetry', site_id)
  ON CONFLICT (serial_number)
    DO NOTHING;
  -- Create sample EV Charger device
  INSERT INTO devices(serial_number, name, description, device_type_id, model, manufacturer, status, rated_power_kw, mqtt_topic, site_id)
    VALUES ('EV001-NY', 'EV Charging Station 1', 'Fast charging station for electric vehicles', ev_type_id, 'ChargePoint Express 250', 'ChargePoint', 'ONLINE', 150.0, 'ecogrid/sites/1/devices/3/telemetry', site_id)
  ON CONFLICT (serial_number)
    DO NOTHING;
END
$$;

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_device_site_status ON devices(site_id, status);

CREATE INDEX IF NOT EXISTS idx_device_mqtt_topic ON devices(mqtt_topic);

COMMIT;

