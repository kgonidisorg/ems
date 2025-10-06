-- EMS Database Seeding Script
-- This script creates comprehensive test data for end-to-end WebSocket testing
-- Run this script against PostgreSQL databases for both auth-service and device-service
-- =============================================================================
-- AUTH SERVICE DATABASE SEEDING (ems_auth)
-- =============================================================================
\c ems_auth;
-- Clear existing users
DELETE FROM users;

-- Create test users with different roles
INSERT INTO users(email, password_hash, first_name, last_name, role, account_enabled, account_locked, credentials_expired, created_at, updated_at)
  VALUES
    -- Admin users
('admin@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Admin', 'User', 'ADMIN', TRUE, FALSE, FALSE, NOW(), NOW()),
('superadmin@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Super', 'Admin', 'ADMIN', TRUE, FALSE, FALSE, NOW(), NOW()),
    -- Operator users
('operator1@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'John', 'Smith', 'OPERATOR', TRUE, FALSE, FALSE, NOW(), NOW()),
('operator2@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Sarah', 'Johnson', 'OPERATOR', TRUE, FALSE, FALSE, NOW(), NOW()),
('operator3@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Mike', 'Davis', 'OPERATOR', TRUE, FALSE, FALSE, NOW(), NOW()),
    -- Viewer users
('viewer1@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Emily', 'Brown', 'VIEWER', TRUE, FALSE, FALSE, NOW(), NOW()),
('viewer2@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Tom', 'Wilson', 'VIEWER', TRUE, FALSE, FALSE, NOW(), NOW()),
    -- Test user for demos (password: "password123")
('test@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Test', 'User', 'OPERATOR', TRUE, FALSE, FALSE, NOW(), NOW());

-- =============================================================================
-- DEVICE SERVICE DATABASE SEEDING (ems_devices)
-- =============================================================================
\c ems_devices;
-- Clear existing data (order matters due to foreign keys)
DELETE FROM device_metadata;

DELETE FROM device_configuration;

DELETE FROM devices;

DELETE FROM sites;

DELETE FROM device_types;

-- Create device types with telemetry schemas
INSERT INTO device_types(name, category, telemetry_schema, alert_thresholds, specifications, created_at)
  VALUES ('BMS', 'STORAGE', '{
  "soc": {"type": "number", "unit": "%", "min": 0, "max": 100},
  "voltage": {"type": "number", "unit": "V", "min": 0, "max": 1000},
  "current": {"type": "number", "unit": "A", "min": -500, "max": 500},
  "temperature": {"type": "number", "unit": "°C", "min": -20, "max": 60},
  "power": {"type": "number", "unit": "kW", "min": -1000, "max": 1000},
  "energy_today": {"type": "number", "unit": "kWh", "min": 0},
  "cycle_count": {"type": "integer", "min": 0},
  "health": {"type": "number", "unit": "%", "min": 0, "max": 100}
}', '{
  "soc_low": 20,
  "soc_high": 95,
  "temperature_high": 45,
  "voltage_low": 600,
  "voltage_high": 850
}', '{
  "capacity_kwh": 2400,
  "max_power_kw": 1000,
  "efficiency": 0.95,
  "chemistry": "LiFePO4"
}', NOW()),
('SOLAR_ARRAY', 'GENERATION', '{
  "power": {"type": "number", "unit": "kW", "min": 0, "max": 2000},
  "voltage": {"type": "number", "unit": "V", "min": 0, "max": 1000},
  "current": {"type": "number", "unit": "A", "min": 0, "max": 100},
  "temperature": {"type": "number", "unit": "°C", "min": -20, "max": 80},
  "irradiance": {"type": "number", "unit": "W/m²", "min": 0, "max": 1200},
  "energy_today": {"type": "number", "unit": "kWh", "min": 0},
  "efficiency": {"type": "number", "unit": "%", "min": 0, "max": 25}
}', '{
  "temperature_high": 65,
  "power_low_threshold": 50,
  "efficiency_low": 15
}', '{
  "capacity_kw": 1800,
  "panel_count": 48,
  "panel_type": "Monocrystalline",
  "inverter_type": "String"
}', NOW()),
('EV_CHARGER', 'CHARGING', '{
  "power": {"type": "number", "unit": "kW", "min": 0, "max": 350},
  "voltage": {"type": "number", "unit": "V", "min": 0, "max": 1000},
  "current": {"type": "number", "unit": "A", "min": 0, "max": 500},
  "energy_delivered": {"type": "number", "unit": "kWh", "min": 0},
  "active_sessions": {"type": "integer", "min": 0, "max": 10},
  "temperature": {"type": "number", "unit": "°C", "min": -20, "max": 60},
  "status": {"type": "string", "enum": ["AVAILABLE", "CHARGING", "FAULTED", "OFFLINE"]}
}', '{
  "temperature_high": 50,
  "power_max": 350,
  "session_timeout": 480
}', '{
  "max_power_kw": 350,
  "connector_type": "CCS",
  "port_count": 8,
  "payment_methods": ["RFID", "APP", "CREDIT_CARD"]
}', NOW());

-- Create 50 sites across different US locations
INSERT INTO sites(name, description, location_lat, location_lng, capacity_mw, status, timezone, address, contact_person, contact_email, contact_phone, created_at, updated_at)
  VALUES
  -- Major MQTT Test Sites (10 sites - these will receive MQTT telemetry)
('New York Corporate HQ', 'Primary corporate headquarters with comprehensive EMS', 40.7128, -74.0060, 5.2, 'ACTIVE', 'America/New_York', '123 Energy Plaza, New York, NY 10001', 'John Smith', 'john.smith@ecogrid.com', '+1-212-555-0001', NOW(), NOW()),
('Los Angeles Solar Farm', 'Large-scale solar installation with battery storage', 34.0522, -118.2437, 8.5, 'ACTIVE', 'America/Los_Angeles', '456 Solar Avenue, Los Angeles, CA 90210', 'Maria Garcia', 'maria.garcia@ecogrid.com', '+1-323-555-0002', NOW(), NOW()),
('Chicago Industrial Complex', 'Manufacturing facility with microgrid', 41.8781, -87.6298, 3.8, 'ACTIVE', 'America/Chicago', '789 Industrial Blvd, Chicago, IL 60601', 'Robert Johnson', 'robert.johnson@ecogrid.com', '+1-312-555-0003', NOW(), NOW()),
('Houston Energy Hub', 'Major energy storage and distribution center', 29.7604, -95.3698, 12.3, 'ACTIVE', 'America/Chicago', '321 Energy Way, Houston, TX 77001', 'Lisa Chen', 'lisa.chen@ecogrid.com', '+1-713-555-0004', NOW(), NOW()),
('Phoenix Solar Park', 'Desert solar installation with EV charging', 33.4484, -112.0740, 6.7, 'ACTIVE', 'America/Phoenix', '654 Desert Drive, Phoenix, AZ 85001', 'David Miller', 'david.miller@ecogrid.com', '+1-602-555-0005', NOW(), NOW()),
('Miami Beach Resort', 'Luxury resort with sustainable energy systems', 25.7617, -80.1918, 2.1, 'ACTIVE', 'America/New_York', '987 Ocean Drive, Miami Beach, FL 33139', 'Sofia Rodriguez', 'sofia.rodriguez@ecogrid.com', '+1-305-555-0006', NOW(), NOW()),
('Seattle Tech Campus', 'Technology campus with advanced energy management', 47.6062, -122.3321, 4.5, 'ACTIVE', 'America/Los_Angeles', '147 Tech Boulevard, Seattle, WA 98101', 'Kevin Park', 'kevin.park@ecogrid.com', '+1-206-555-0007', NOW(), NOW()),
('Denver Mountain Facility', 'High-altitude renewable energy testing facility', 39.7392, -104.9903, 3.2, 'ACTIVE', 'America/Denver', '258 Mountain View Road, Denver, CO 80201', 'Amanda White', 'amanda.white@ecogrid.com', '+1-303-555-0008', NOW(), NOW()),
('Atlanta Distribution Center', 'Major logistics hub with comprehensive EMS', 33.7490, -84.3880, 7.8, 'ACTIVE', 'America/New_York', '369 Logistics Lane, Atlanta, GA 30301', 'Michael Brown', 'michael.brown@ecogrid.com', '+1-404-555-0009', NOW(), NOW()),
('Portland Green Campus', 'Sustainable business park with microgrid', 45.5152, -122.6784, 5.9, 'ACTIVE', 'America/Los_Angeles', '741 Green Street, Portland, OR 97201', 'Jennifer Davis', 'jennifer.davis@ecogrid.com', '+1-503-555-0010', NOW(), NOW()),
  -- Additional Sites (40 more sites - monitoring only)
('Boston Medical Center', 'Hospital complex with backup power systems', 42.3601, -71.0589, 4.2, 'ACTIVE', 'America/New_York', '852 Medical Drive, Boston, MA 02101', 'Dr. Sarah Wilson', 'sarah.wilson@ecogrid.com', '+1-617-555-0011', NOW(), NOW()),
('San Francisco Tech Hub', 'Technology incubator with renewable energy', 37.7749, -122.4194, 3.1, 'ACTIVE', 'America/Los_Angeles', '963 Innovation Way, San Francisco, CA 94101', 'Alex Thompson', 'alex.thompson@ecogrid.com', '+1-415-555-0012', NOW(), NOW()),
('Las Vegas Entertainment Complex', 'Casino and hotel with large energy demand', 36.1699, -115.1398, 8.9, 'ACTIVE', 'America/Los_Angeles', '159 Strip Boulevard, Las Vegas, NV 89101', 'Tony Martinez', 'tony.martinez@ecogrid.com', '+1-702-555-0013', NOW(), NOW()),
('Detroit Manufacturing Plant', 'Automotive manufacturing with energy optimization', 42.3314, -83.0458, 6.4, 'ACTIVE', 'America/New_York', '357 Auto Lane, Detroit, MI 48201', 'Linda Jackson', 'linda.jackson@ecogrid.com', '+1-313-555-0014', NOW(), NOW()),
('Nashville Music District', 'Entertainment district with smart grid', 36.1627, -86.7816, 2.8, 'ACTIVE', 'America/Chicago', '456 Music Row, Nashville, TN 37201', 'Country Smith', 'country.smith@ecogrid.com', '+1-615-555-0015', NOW(), NOW()),
('Orlando Theme Park', 'Major theme park with renewable energy systems', 28.5383, -81.3792, 15.2, 'ACTIVE', 'America/New_York', '758 Magic Kingdom Way, Orlando, FL 32801', 'Disney Manager', 'disney.manager@ecogrid.com', '+1-407-555-0016', NOW(), NOW()),
('Salt Lake City Data Center', 'Large data center with cooling optimization', 40.7608, -111.8910, 5.5, 'ACTIVE', 'America/Denver', '951 Data Drive, Salt Lake City, UT 84101', 'Tech Admin', 'tech.admin@ecogrid.com', '+1-801-555-0017', NOW(), NOW()),
('Kansas City Logistics Hub', 'Central distribution facility', 39.0997, -94.5786, 4.1, 'ACTIVE', 'America/Chicago', '753 Central Avenue, Kansas City, MO 64101', 'Hub Manager', 'hub.manager@ecogrid.com', '+1-816-555-0018', NOW(), NOW()),
('San Diego Research Facility', 'Renewable energy research and development', 32.7157, -117.1611, 3.7, 'ACTIVE', 'America/Los_Angeles', '852 Research Blvd, San Diego, CA 92101', 'Dr. Research', 'dr.research@ecogrid.com', '+1-619-555-0019', NOW(), NOW()),
('Minneapolis Cold Storage', 'Refrigerated warehouse with energy management', 44.9778, -93.2650, 6.8, 'ACTIVE', 'America/Chicago', '147 Cold Street, Minneapolis, MN 55401', 'Cold Manager', 'cold.manager@ecogrid.com', '+1-612-555-0020', NOW(), NOW()),
('Tampa Bay Industrial', 'Industrial complex with cogeneration', 27.9506, -82.4572, 7.3, 'ACTIVE', 'America/New_York', '258 Industrial Way, Tampa, FL 33601', 'Industrial Lead', 'industrial.lead@ecogrid.com', '+1-813-555-0021', NOW(), NOW()),
('Sacramento Government Complex', 'State government facilities with efficiency focus', 38.5816, -121.4944, 4.9, 'ACTIVE', 'America/Los_Angeles', '369 Capitol Mall, Sacramento, CA 95814', 'Gov Official', 'gov.official@ecogrid.com', '+1-916-555-0022', NOW(), NOW()),
('Cleveland Manufacturing Hub', 'Heavy manufacturing with energy recovery', 41.4993, -81.6944, 8.2, 'ACTIVE', 'America/New_York', '741 Manufacturing Row, Cleveland, OH 44101', 'Mfg Director', 'mfg.director@ecogrid.com', '+1-216-555-0023', NOW(), NOW()),
('San Antonio Military Base', 'Military installation with secure energy systems', 29.4241, -98.4936, 12.5, 'ACTIVE', 'America/Chicago', '852 Base Road, San Antonio, TX 78201', 'Base Commander', 'base.commander@ecogrid.com', '+1-210-555-0024', NOW(), NOW()),
('Pittsburgh Steel Works', 'Steel production facility with waste heat recovery', 40.4406, -79.9959, 18.7, 'ACTIVE', 'America/New_York', '963 Steel Avenue, Pittsburgh, PA 15201', 'Steel Foreman', 'steel.foreman@ecogrid.com', '+1-412-555-0025', NOW(), NOW()),
('Cincinnati Riverfront', 'Mixed-use development with microgrid', 39.1031, -84.5120, 3.4, 'ACTIVE', 'America/New_York', '159 River Walk, Cincinnati, OH 45201', 'Development Manager', 'dev.manager@ecogrid.com', '+1-513-555-0026', NOW(), NOW()),
('Milwaukee Brewery District', 'Historic brewery district with modern energy systems', 43.0389, -87.9065, 2.6, 'ACTIVE', 'America/Chicago', '357 Brewery Lane, Milwaukee, WI 53201', 'Brew Master', 'brew.master@ecogrid.com', '+1-414-555-0027', NOW(), NOW()),
('Albuquerque Research Lab', 'National laboratory with advanced energy research', 35.0844, -106.6504, 5.1, 'ACTIVE', 'America/Denver', '456 Lab Drive, Albuquerque, NM 87101', 'Lab Director', 'lab.director@ecogrid.com', '+1-505-555-0028', NOW(), NOW()),
('Fresno Agricultural Center', 'Agricultural processing with solar systems', 36.7378, -119.7871, 4.8, 'ACTIVE', 'America/Los_Angeles', '758 Farm Road, Fresno, CA 93701', 'Ag Manager', 'ag.manager@ecogrid.com', '+1-559-555-0029', NOW(), NOW()),
('Tucson Desert Solar', 'Large desert solar installation', 32.2226, -110.9747, 25.3, 'ACTIVE', 'America/Phoenix', '951 Desert Highway, Tucson, AZ 85701', 'Solar Chief', 'solar.chief@ecogrid.com', '+1-520-555-0030', NOW(), NOW()),
('Virginia Beach Resort Complex', 'Coastal resort with wind and solar systems', 36.8529, -75.9780, 6.2, 'ACTIVE', 'America/New_York', '753 Ocean Front, Virginia Beach, VA 23451', 'Resort Manager', 'resort.manager@ecogrid.com', '+1-757-555-0031', NOW(), NOW()),
('Omaha Food Processing', 'Large food processing facility', 41.2565, -95.9345, 7.9, 'ACTIVE', 'America/Chicago', '852 Food Street, Omaha, NE 68101', 'Food Director', 'food.director@ecogrid.com', '+1-402-555-0032', NOW(), NOW()),
('Raleigh Research Triangle', 'Technology research park', 35.7796, -78.6382, 8.4, 'ACTIVE', 'America/New_York', '147 Research Drive, Raleigh, NC 27601', 'Research Head', 'research.head@ecogrid.com', '+1-919-555-0033', NOW(), NOW()),
('Boise Tech Campus', 'Technology company headquarters', 43.6150, -116.2023, 3.8, 'ACTIVE', 'America/Denver', '258 Tech Boulevard, Boise, ID 83701', 'Tech CEO', 'tech.ceo@ecogrid.com', '+1-208-555-0034', NOW(), NOW()),
('Des Moines Insurance Center', 'Insurance company headquarters with efficiency focus', 41.5868, -93.6250, 4.7, 'ACTIVE', 'America/Chicago', '369 Insurance Way, Des Moines, IA 50301', 'Insurance VP', 'insurance.vp@ecogrid.com', '+1-515-555-0035', NOW(), NOW()),
('Charleston Historic District', 'Historic preservation with modern energy systems', 32.7765, -79.9311, 2.1, 'ACTIVE', 'America/New_York', '741 Historic Lane, Charleston, SC 29401', 'Historic Curator', 'historic.curator@ecogrid.com', '+1-843-555-0036', NOW(), NOW()),
('Little Rock Government Center', 'State facilities with energy management', 34.7465, -92.2896, 5.3, 'ACTIVE', 'America/Chicago', '852 Capitol Avenue, Little Rock, AR 72201', 'State Manager', 'state.manager@ecogrid.com', '+1-501-555-0037', NOW(), NOW()),
('Jackson Medical Complex', 'Large medical center with backup systems', 32.2988, -90.1848, 6.9, 'ACTIVE', 'America/Chicago', '963 Medical Plaza, Jackson, MS 39201', 'Medical Director', 'medical.director@ecogrid.com', '+1-601-555-0038', NOW(), NOW()),
('Columbia University District', 'University campus with research facilities', 38.9517, -92.3341, 4.2, 'ACTIVE', 'America/Chicago', '159 University Circle, Columbia, MO 65201', 'Campus Director', 'campus.director@ecogrid.com', '+1-573-555-0039', NOW(), NOW()),
('Augusta Golf Resort', 'Luxury golf resort with sustainable systems', 33.4735, -82.0105, 1.8, 'ACTIVE', 'America/New_York', '357 Golf Course Road, Augusta, GA 30901', 'Golf Manager', 'golf.manager@ecogrid.com', '+1-706-555-0040', NOW(), NOW()),
('Spokane Manufacturing', 'Aerospace manufacturing facility', 47.6587, -117.4260, 9.1, 'ACTIVE', 'America/Los_Angeles', '456 Aerospace Drive, Spokane, WA 99201', 'Aero Director', 'aero.director@ecogrid.com', '+1-509-555-0041', NOW(), NOW()),
('Fort Wayne Industrial', 'Heavy industry with cogeneration systems', 41.0793, -85.1394, 11.2, 'ACTIVE', 'America/New_York', '758 Industrial Parkway, Fort Wayne, IN 46801', 'Industrial VP', 'industrial.vp@ecogrid.com', '+1-260-555-0042', NOW(), NOW()),
('Shreveport Energy Plant', 'Natural gas peaker plant with storage', 32.5252, -93.7502, 15.7, 'ACTIVE', 'America/Chicago', '951 Energy Road, Shreveport, LA 71101', 'Plant Manager', 'plant.manager@ecogrid.com', '+1-318-555-0043', NOW(), NOW()),
('Grand Rapids Furniture District', 'Furniture manufacturing with wood waste energy', 42.9634, -85.6681, 6.8, 'ACTIVE', 'America/New_York', '753 Furniture Street, Grand Rapids, MI 49501', 'Furniture CEO', 'furniture.ceo@ecogrid.com', '+1-616-555-0044', NOW(), NOW()),
('Huntsville Space Center', 'Aerospace research with advanced energy systems', 34.7304, -86.5861, 8.9, 'ACTIVE', 'America/Chicago', '852 Rocket Road, Huntsville, AL 35801', 'Space Director', 'space.director@ecogrid.com', '+1-256-555-0045', NOW(), NOW()),
('Fargo Agricultural Processing', 'Large grain processing facility', 46.8772, -96.7898, 12.4, 'ACTIVE', 'America/Chicago', '147 Grain Avenue, Fargo, ND 58101', 'Grain Manager', 'grain.manager@ecogrid.com', '+1-701-555-0046', NOW(), NOW()),
('Sioux Falls Financial Center', 'Major credit card processing center', 43.5446, -96.7311, 5.6, 'ACTIVE', 'America/Chicago', '258 Financial Plaza, Sioux Falls, SD 57101', 'Finance Director', 'finance.director@ecogrid.com', '+1-605-555-0047', NOW(), NOW()),
('Burlington Logistics Hub', 'Major transportation and logistics center', 44.4759, -73.2121, 7.1, 'ACTIVE', 'America/New_York', '369 Logistics Boulevard, Burlington, VT 05401', 'Logistics Chief', 'logistics.chief@ecogrid.com', '+1-802-555-0048', NOW(), NOW()),
('Portland Maine Port', 'Shipping port with shore power systems', 43.6591, -70.2568, 4.3, 'ACTIVE', 'America/New_York', '741 Harbor Drive, Portland, ME 04101', 'Port Authority', 'port.authority@ecogrid.com', '+1-207-555-0049', NOW(), NOW()),
('Anchorage Arctic Research', 'Arctic research facility with renewable energy', 61.2181, -149.9003, 3.5, 'ACTIVE', 'America/Anchorage', '852 Arctic Circle, Anchorage, AK 99501', 'Arctic Researcher', 'arctic.researcher@ecogrid.com', '+1-907-555-0050', NOW(), NOW());

-- Create devices for the first 10 sites (MQTT test sites)
-- Each site will have: 2 BMS units, 1 Solar Array, 1 EV Charger
-- New York Corporate HQ (Site 1) - 4 devices
INSERT INTO devices(serial_number, name, description, device_type_id, model, manufacturer, status, mqtt_topic, ip_address, mac_address, installation_date, site_id, created_at, updated_at)
  VALUES ('BMS-NY-001', 'Battery Management System 1', 'Primary BMS for building power backup', 1, 'PowerMax Pro 2400', 'Tesla Energy', 'ONLINE', 'ecogrid/site1/bms/001', '192.168.1.101', '00:1B:44:11:3A:B7', '2024-01-15', 1, NOW(), NOW()),
('BMS-NY-002', 'Battery Management System 2', 'Secondary BMS for load balancing', 1, 'PowerMax Pro 2400', 'Tesla Energy', 'ONLINE', 'ecogrid/site1/bms/002', '192.168.1.102', '00:1B:44:11:3A:B8', '2024-01-15', 1, NOW(), NOW()),
('SOLAR-NY-001', 'Rooftop Solar Array', 'Primary solar installation on building roof', 2, 'SolarEdge SE25K', 'SolarEdge', 'ONLINE', 'ecogrid/site1/solar/001', '192.168.1.201', '00:1B:44:11:3C:D1', '2024-01-10', 1, NOW(), NOW()),
('EV-NY-001', 'EV Charging Station', 'Employee parking lot charging station', 3, 'Supercharger V3', 'Tesla', 'ONLINE', 'ecogrid/site1/ev/001', '192.168.1.301', '00:1B:44:11:3D:E2', '2024-01-20', 1, NOW(), NOW()),
  -- Los Angeles Solar Farm (Site 2) - 4 devices
('BMS-LA-001', 'Battery Storage Unit 1', 'Primary utility-scale battery storage', 1, 'Megapack 2XL', 'Tesla Energy', 'ONLINE', 'ecogrid/site2/bms/001', '192.168.2.101', '00:1B:44:22:4A:C7', '2024-02-01', 2, NOW(), NOW()),
('BMS-LA-002', 'Battery Storage Unit 2', 'Secondary utility-scale battery storage', 1, 'Megapack 2XL', 'Tesla Energy', 'ONLINE', 'ecogrid/site2/bms/002', '192.168.2.102', '00:1B:44:22:4A:C8', '2024-02-01', 2, NOW(), NOW()),
('SOLAR-LA-001', 'Solar Farm Array', 'Main 8.5MW solar photovoltaic array', 2, 'Utility Scale Tracker', 'First Solar', 'ONLINE', 'ecogrid/site2/solar/001', '192.168.2.201', '00:1B:44:22:4C:F1', '2024-01-25', 2, NOW(), NOW()),
('EV-LA-001', 'Public Charging Plaza', 'Public EV charging facility', 3, 'ChargePoint Express Plus', 'ChargePoint', 'ONLINE', 'ecogrid/site2/ev/001', '192.168.2.301', '00:1B:44:22:4D:G2', '2024-02-10', 2, NOW(), NOW()),
  -- Chicago Industrial Complex (Site 3) - 4 devices
('BMS-CHI-001', 'Industrial Battery Bank 1', 'Manufacturing backup power system', 1, 'Industrial PowerPack', 'LG Chem', 'ONLINE', 'ecogrid/site3/bms/001', '192.168.3.101', '00:1B:44:33:5A:H7', '2024-01-30', 3, NOW(), NOW()),
('BMS-CHI-002', 'Industrial Battery Bank 2', 'Load shifting and peak shaving system', 1, 'Industrial PowerPack', 'LG Chem', 'ONLINE', 'ecogrid/site3/bms/002', '192.168.3.102', '00:1B:44:33:5A:H8', '2024-01-30', 3, NOW(), NOW()),
('SOLAR-CHI-001', 'Factory Roof Solar', 'Industrial rooftop solar installation', 2, 'Commercial Inverter', 'SMA America', 'ONLINE', 'ecogrid/site3/solar/001', '192.168.3.201', '00:1B:44:33:5C:J1', '2024-01-25', 3, NOW(), NOW()),
('EV-CHI-001', 'Fleet Charging Depot', 'Industrial vehicle charging facility', 3, 'Heavy Duty Charger', 'ABB', 'ONLINE', 'ecogrid/site3/ev/001', '192.168.3.301', '00:1B:44:33:5D:K2', '2024-02-05', 3, NOW(), NOW()),
  -- Houston Energy Hub (Site 4) - 4 devices
('BMS-HOU-001', 'Grid Storage System 1', 'Utility grid stabilization battery', 1, 'Grid Scale ESS', 'Fluence', 'ONLINE', 'ecogrid/site4/bms/001', '192.168.4.101', '00:1B:44:44:6A:L7', '2024-02-15', 4, NOW(), NOW()),
('BMS-HOU-002', 'Grid Storage System 2', 'Frequency regulation battery system', 1, 'Grid Scale ESS', 'Fluence', 'ONLINE', 'ecogrid/site4/bms/002', '192.168.4.102', '00:1B:44:44:6A:L8', '2024-02-15', 4, NOW(), NOW()),
('SOLAR-HOU-001', 'Central Solar Plant', 'Large-scale solar generation facility', 2, 'Central Inverter', 'Power Electronics', 'ONLINE', 'ecogrid/site4/solar/001', '192.168.4.201', '00:1B:44:44:6C:M1', '2024-02-10', 4, NOW(), NOW()),
('EV-HOU-001', 'Highway Charging Hub', 'Interstate highway charging station', 3, 'Electrify America', 'Electrify America', 'ONLINE', 'ecogrid/site4/ev/001', '192.168.4.301', '00:1B:44:44:6D:N2', '2024-02-20', 4, NOW(), NOW()),
  -- Phoenix Solar Park (Site 5) - 4 devices
('BMS-PHX-001', 'Desert Storage Unit 1', 'High-temperature rated battery system', 1, 'Desert Series ESS', 'Powin Energy', 'ONLINE', 'ecogrid/site5/bms/001', '192.168.5.101', '00:1B:44:55:7A:O7', '2024-03-01', 5, NOW(), NOW()),
('BMS-PHX-002', 'Desert Storage Unit 2', 'Thermal management optimized storage', 1, 'Desert Series ESS', 'Powin Energy', 'ONLINE', 'ecogrid/site5/bms/002', '192.168.5.102', '00:1B:44:55:7A:O8', '2024-03-01', 5, NOW(), NOW()),
('SOLAR-PHX-001', 'Desert Solar Array', 'High-efficiency desert solar installation', 2, 'Bifacial Tracker', 'NEXTracker', 'ONLINE', 'ecogrid/site5/solar/001', '192.168.5.201', '00:1B:44:55:7C:P1', '2024-02-25', 5, NOW(), NOW()),
('EV-PHX-001', 'Desert Oasis Chargers', 'Solar-powered EV charging oasis', 3, 'Solar Canopy Charger', 'Beam Global', 'ONLINE', 'ecogrid/site5/ev/001', '192.168.5.301', '00:1B:44:55:7D:Q2', '2024-03-05', 5, NOW(), NOW()),
  -- Miami Beach Resort (Site 6) - 4 devices
('BMS-MIA-001', 'Resort Battery System 1', 'Hurricane-resistant backup power', 1, 'Storm Series Battery', 'Sunnova', 'ONLINE', 'ecogrid/site6/bms/001', '192.168.6.101', '00:1B:44:66:8A:R7', '2024-03-10', 6, NOW(), NOW()),
('BMS-MIA-002', 'Resort Battery System 2', 'Guest facility power backup', 1, 'Storm Series Battery', 'Sunnova', 'ONLINE', 'ecogrid/site6/bms/002', '192.168.6.102', '00:1B:44:66:8A:R8', '2024-03-10', 6, NOW(), NOW()),
('SOLAR-MIA-001', 'Beach Resort Solar', 'Corrosion-resistant coastal solar array', 2, 'Marine Grade System', 'SunPower', 'ONLINE', 'ecogrid/site6/solar/001', '192.168.6.201', '00:1B:44:66:8C:S1', '2024-03-05', 6, NOW(), NOW()),
('EV-MIA-001', 'Valet Charging Service', 'Luxury resort EV charging valet', 3, 'Hospitality Charger', 'FreeWire', 'ONLINE', 'ecogrid/site6/ev/001', '192.168.6.301', '00:1B:44:66:8D:T2', '2024-03-15', 6, NOW(), NOW()),
  -- Seattle Tech Campus (Site 7) - 4 devices
('BMS-SEA-001', 'Campus Microgrid Battery 1', 'Smart campus energy storage', 1, 'Smart Grid ESS', 'Stem Inc', 'ONLINE', 'ecogrid/site7/bms/001', '192.168.7.101', '00:1B:44:77:9A:U7', '2024-03-20', 7, NOW(), NOW()),
('BMS-SEA-002', 'Campus Microgrid Battery 2', 'AI-optimized energy management', 1, 'Smart Grid ESS', 'Stem Inc', 'ONLINE', 'ecogrid/site7/bms/002', '192.168.7.102', '00:1B:44:77:9A:U8', '2024-03-20', 7, NOW(), NOW()),
('SOLAR-SEA-001', 'Tech Campus Solar', 'Integrated building solar system', 2, 'Building Integrated PV', 'CertainTeed', 'ONLINE', 'ecogrid/site7/solar/001', '192.168.7.201', '00:1B:44:77:9C:V1', '2024-03-15', 7, NOW(), NOW()),
('EV-SEA-001', 'Employee EV Fleet', 'Corporate fleet charging infrastructure', 3, 'Workplace Charger', 'ChargePoint', 'ONLINE', 'ecogrid/site7/ev/001', '192.168.7.301', '00:1B:44:77:9D:W2', '2024-03-25', 7, NOW(), NOW()),
  -- Denver Mountain Facility (Site 8) - 4 devices
('BMS-DEN-001', 'Mountain Research Battery 1', 'High-altitude testing system', 1, 'Alpine Series ESS', 'Corvus Energy', 'ONLINE', 'ecogrid/site8/bms/001', '192.168.8.101', '00:1B:44:88:AA:X7', '2024-04-01', 8, NOW(), NOW()),
('BMS-DEN-002', 'Mountain Research Battery 2', 'Cold weather optimized storage', 1, 'Alpine Series ESS', 'Corvus Energy', 'ONLINE', 'ecogrid/site8/bms/002', '192.168.8.102', '00:1B:44:88:AA:X8', '2024-04-01', 8, NOW(), NOW()),
('SOLAR-DEN-001', 'Alpine Solar Test Array', 'High-altitude solar research system', 2, 'Research Tracker', 'Array Technologies', 'ONLINE', 'ecogrid/site8/solar/001', '192.168.8.201', '00:1B:44:88:AC:Y1', '2024-03-28', 8, NOW(), NOW()),
('EV-DEN-001', 'Mountain Access Charger', 'Remote location EV charging', 3, 'Off-Grid Charger', 'Paired Power', 'ONLINE', 'ecogrid/site8/ev/001', '192.168.8.301', '00:1B:44:88:AD:Z2', '2024-04-05', 8, NOW(), NOW()),
  -- Atlanta Distribution Center (Site 9) - 4 devices
('BMS-ATL-001', 'Logistics Hub Battery 1', 'Warehouse energy management system', 1, 'Logistics ESS', 'Nuvation Energy', 'ONLINE', 'ecogrid/site9/bms/001', '192.168.9.101', '00:1B:44:99:BA:A7', '2024-04-10', 9, NOW(), NOW()),
('BMS-ATL-002', 'Logistics Hub Battery 2', 'Peak demand management system', 1, 'Logistics ESS', 'Nuvation Energy', 'ONLINE', 'ecogrid/site9/bms/002', '192.168.9.102', '00:1B:44:99:BA:A8', '2024-04-10', 9, NOW(), NOW()),
('SOLAR-ATL-001', 'Distribution Center Solar', 'Large warehouse rooftop solar', 2, 'Commercial Rooftop', 'Enphase Energy', 'ONLINE', 'ecogrid/site9/solar/001', '192.168.9.201', '00:1B:44:99:BC:B1', '2024-04-05', 9, NOW(), NOW()),
('EV-ATL-001', 'Delivery Fleet Chargers', 'Electric delivery vehicle charging', 3, 'Fleet Charger', 'Workhorse', 'ONLINE', 'ecogrid/site9/ev/001', '192.168.9.301', '00:1B:44:99:BD:C2', '2024-04-15', 9, NOW(), NOW()),
  -- Portland Green Campus (Site 10) - 4 devices
('BMS-PDX-001', 'Green Campus Battery 1', 'Sustainable energy storage system', 1, 'Eco Series ESS', 'SimpliPhi Power', 'ONLINE', 'ecogrid/site10/bms/001', '192.168.10.101', '00:1B:44:AA:CA:D7', '2024-04-20', 10, NOW(), NOW()),
('BMS-PDX-002', 'Green Campus Battery 2', 'Carbon-neutral energy management', 1, 'Eco Series ESS', 'SimpliPhi Power', 'ONLINE', 'ecogrid/site10/bms/002', '192.168.10.102', '00:1B:44:AA:CA:D8', '2024-04-20', 10, NOW(), NOW()),
('SOLAR-PDX-001', 'Sustainable Campus Solar', 'LEED certified solar installation', 2, 'Sustainable Tracker', 'GameChange Solar', 'ONLINE', 'ecogrid/site10/solar/001', '192.168.10.201', '00:1B:44:AA:CC:E1', '2024-04-15', 10, NOW(), NOW()),
('EV-PDX-001', 'Green Transportation Hub', 'Renewable-powered EV charging', 3, 'Green Charger', 'EV Connect', 'ONLINE', 'ecogrid/site10/ev/001', '192.168.10.301', '00:1B:44:AA:CD:F2', '2024-04-25', 10, NOW(), NOW());

-- Add configuration and metadata for the devices
INSERT INTO device_configuration(device_id, config_key, config_value)
  VALUES
    -- BMS configurations
(1, 'max_soc', '95'),
(1, 'min_soc', '20'),
(1, 'max_power_kw', '1000'),
(2, 'max_soc', '95'),
(2, 'min_soc', '20'),
(2, 'max_power_kw', '1000'),
(5, 'max_soc', '90'),
(5, 'min_soc', '10'),
(5, 'max_power_kw', '2500'),
(6, 'max_soc', '90'),
(6, 'min_soc', '10'),
(6, 'max_power_kw', '2500'),
    -- Solar configurations
(3, 'max_power_kw', '1800'),
(3, 'panel_count', '48'),
(3, 'inverter_count', '3'),
(7, 'max_power_kw', '8500'),
(7, 'panel_count', '20000'),
(7, 'inverter_count', '50'),
    -- EV Charger configurations
(4, 'max_power_kw', '250'),
(4, 'port_count', '8'),
(4, 'connector_type', 'CCS'),
(8, 'max_power_kw', '350'),
(8, 'port_count', '12'),
(8, 'connector_type', 'CCS');

INSERT INTO device_metadata(device_id, meta_key, meta_value)
  VALUES
    -- Device metadata for monitoring and alerts
(1, 'warranty_date', '2029-01-15'),
(1, 'last_maintenance', '2024-09-01'),
(2, 'warranty_date', '2029-01-15'),
(2, 'last_maintenance', '2024-09-01'),
(3, 'warranty_date', '2034-01-10'),
(3, 'cleaning_schedule', 'monthly'),
(4, 'warranty_date', '2032-01-20'),
(4, 'network_provider', 'Verizon'),
(5, 'warranty_date', '2029-02-01'),
(5, 'cooling_type', 'liquid'),
(6, 'warranty_date', '2029-02-01'),
(6, 'cooling_type', 'liquid'),
(7, 'warranty_date', '2034-01-25'),
(7, 'tracker_type', 'single_axis'),
(8, 'warranty_date', '2032-02-10'),
(8, 'payment_methods', 'rfid,app,credit');

-- Print summary
SELECT
  'Database seeding completed successfully!' AS status;

SELECT
  'Users created: ' || COUNT(*) AS user_count
FROM
  users;

SELECT
  'Sites created: ' || COUNT(*) AS site_count
FROM
  sites;

SELECT
  'Device types created: ' || COUNT(*) AS device_type_count
FROM
  device_types;

SELECT
  'Devices created: ' || COUNT(*) AS device_count
FROM
  devices;

SELECT
  'MQTT-enabled sites (first 10): ' || COUNT(*) AS mqtt_sites
FROM
  sites
WHERE
  id <= 10;

SELECT
  'Total devices with MQTT topics: ' || COUNT(*) AS mqtt_devices
FROM
  devices
WHERE
  mqtt_topic IS NOT NULL;

-- Show test user credentials
SELECT
  'Test User Login Credentials:' AS info;

SELECT
  email AS username,
  'password123' AS password,
  ROLE,
  first_name || ' ' || last_name AS name
FROM
  users
WHERE
  email IN ('test@ecogrid.com', 'admin@ecogrid.com', 'operator1@ecogrid.com')
ORDER BY
  ROLE DESC;

