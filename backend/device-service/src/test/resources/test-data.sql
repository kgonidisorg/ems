-- Test data for integration tests
-- Insert device types
INSERT INTO device_types (name, description, config_schema, created_at, updated_at)
VALUES 
    ('BMS', 'Battery Management System', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Solar Array', 'Solar Panel Array System', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('EV Charger', 'Electric Vehicle Charging Station', '{}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test site
INSERT INTO sites (name, description, address, location, timezone, created_at, updated_at)
VALUES ('Test Site 001', 'Integration test site', '123 Test St', 'POINT(-122.4194 37.7749)', 'America/Los_Angeles', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test devices
INSERT INTO devices (serial_number, name, description, device_type_id, model, manufacturer, firmware_version, 
                    hardware_version, installation_date, warranty_expiry, status, site_id, location, 
                    configuration, metadata, created_at, updated_at)
VALUES 
    ('bms-001', 'Test BMS Device 001', 'Battery Management System for testing', 
     (SELECT id FROM device_types WHERE name = 'BMS'), 
     'BMS-2000', 'EcoGrid', '2.1.0', '1.0', '2024-01-01'::date, '2027-01-01'::date, 'ACTIVE',
     (SELECT id FROM sites WHERE name = 'Test Site 001'), 'Building A - Battery Room',
     '{"capacity": 100, "maxVoltage": 54.0}', '{"installerId": "tech-001"}', 
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
     
    ('solar-001', 'Test Solar Array 001', 'Solar panel array for testing',
     (SELECT id FROM device_types WHERE name = 'Solar Array'),
     'SOLAR-5000', 'SolarTech', '3.2.1', '2.0', '2024-01-01'::date, '2029-01-01'::date, 'ACTIVE',
     (SELECT id FROM sites WHERE name = 'Test Site 001'), 'Rooftop - South Facing',
     '{"panels": 20, "maxPower": 6000}', '{"orientation": "south"}',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
     
    ('evcharger-001', 'Test EV Charger 001', 'Electric vehicle charger for testing',
     (SELECT id FROM device_types WHERE name = 'EV Charger'),
     'EVC-7000', 'ChargePoint', '1.5.2', '1.0', '2024-01-01'::date, '2026-01-01'::date, 'ACTIVE',
     (SELECT id FROM sites WHERE name = 'Test Site 001'), 'Parking Lot - Spot 1',
     '{"maxPower": 7200, "connectors": ["Type2"]}', '{"networkId": "cp-001"}',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);