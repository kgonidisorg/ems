#!/bin/bash

# EMS Database Seeding Script
# This script populates both auth-service and device-service databases with comprehensive test data

set -e  # Exit on any error

# Default database connection parameters
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5433}
DB_USERNAME=${DB_USERNAME:-ems_user}
DB_PASSWORD=${DB_PASSWORD:-ems_password}
AUTH_DB_NAME=${AUTH_DB_NAME:-ems_auth}
DEVICE_DB_NAME=${DEVICE_DB_NAME:-ems_devices}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔧 EMS Database Seeding Script${NC}"
echo "========================================"
echo -e "${YELLOW}Database Configuration:${NC}"
echo "  PostgreSQL: $DB_HOST:$DB_PORT"
echo "  Auth DB: $AUTH_DB_NAME"
echo "  Device DB: $DEVICE_DB_NAME"
echo "  Username: $DB_USERNAME"
echo ""

# Function to execute SQL with proper error handling
execute_sql() {
    local db_name=$1
    local sql_command=$2
    
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $db_name -c "$sql_command"
}

# Function to check if database is accessible
check_database() {
    local db_name=$1
    
    echo -e "${BLUE}Checking connection to $db_name...${NC}"
    if PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $db_name -c "SELECT 1;" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Successfully connected to $db_name${NC}"
        return 0
    else
        echo -e "${RED}✗ Failed to connect to $db_name${NC}"
        return 1
    fi
}

# Check database connections
echo -e "${BLUE}Verifying database connections...${NC}"
if ! check_database $AUTH_DB_NAME; then
    echo -e "${RED}Cannot connect to auth database. Please ensure:${NC}"
    echo "1. PostgreSQL is running on port $DB_PORT"
    echo "2. Auth service database '$AUTH_DB_NAME' is created"
    echo "3. Database credentials are correct"
    exit 1
fi

if ! check_database $DEVICE_DB_NAME; then
    echo -e "${YELLOW}Device database '$DEVICE_DB_NAME' not found. Creating it...${NC}"
    # Try to create the device database
    if PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d postgres -c "CREATE DATABASE $DEVICE_DB_NAME;" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Created device database: $DEVICE_DB_NAME${NC}"
    else
        echo -e "${RED}✗ Failed to create device database. Please create it manually:${NC}"
        echo "CREATE DATABASE $DEVICE_DB_NAME;"
        exit 1
    fi
fi

echo ""
echo -e "${YELLOW}⚠️  WARNING: This will delete all existing data!${NC}"
echo "   - All users will be removed from auth database"
echo "   - All sites, devices, and device types will be removed"
echo ""
read -p "Do you want to continue? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Operation cancelled.${NC}"
    exit 0
fi

echo ""
echo -e "${BLUE}🚀 Starting database seeding...${NC}"

# =============================================================================
# AUTH SERVICE DATABASE SEEDING
# =============================================================================

echo -e "${BLUE}📝 Seeding auth service database...${NC}"

# Clear existing users
echo "  • Clearing existing users..."
execute_sql $AUTH_DB_NAME "DELETE FROM users;"

# Create test users
echo "  • Creating test users..."
execute_sql $AUTH_DB_NAME "
INSERT INTO users (email, password_hash, first_name, last_name, role, account_enabled, account_locked, credentials_expired, created_at, updated_at) VALUES
-- Admin users (password: password123)
('admin@ecogrid.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Admin', 'User', 'ADMIN', true, false, false, NOW(), NOW()),
('superadmin@ecogrid.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Super', 'Admin', 'ADMIN', true, false, false, NOW(), NOW()),
-- Operator users (password: password123)
('operator1@ecogrid.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'John', 'Smith', 'OPERATOR', true, false, false, NOW(), NOW()),
('operator2@ecogrid.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Sarah', 'Johnson', 'OPERATOR', true, false, false, NOW(), NOW()),
('operator3@ecogrid.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Mike', 'Davis', 'OPERATOR', true, false, false, NOW(), NOW()),
-- Viewer users (password: password123)
('viewer1@ecogrid.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Emily', 'Brown', 'VIEWER', true, false, false, NOW(), NOW()),
('viewer2@ecogrid.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Tom', 'Wilson', 'VIEWER', true, false, false, NOW(), NOW()),
-- Test user for demos (password: password123)
('test@ecogrid.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Test', 'User', 'OPERATOR', true, false, false, NOW(), NOW());
"

# Get user count
USER_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $AUTH_DB_NAME -t -c "SELECT COUNT(*) FROM users;")
echo -e "${GREEN}  ✓ Created $USER_COUNT users${NC}"

# =============================================================================
# DEVICE SERVICE DATABASE SEEDING
# =============================================================================

echo -e "${BLUE}📝 Seeding device service database...${NC}"

# Clear existing data (order matters due to foreign keys)
echo "  • Clearing existing data..."
execute_sql $DEVICE_DB_NAME "DELETE FROM device_metadata;"
execute_sql $DEVICE_DB_NAME "DELETE FROM device_configuration;"
execute_sql $DEVICE_DB_NAME "DELETE FROM devices;"
execute_sql $DEVICE_DB_NAME "DELETE FROM sites;"
execute_sql $DEVICE_DB_NAME "DELETE FROM device_types;"

# Create device types
echo "  • Creating device types..."
execute_sql $DEVICE_DB_NAME "
INSERT INTO device_types (name, category, telemetry_schema, alert_thresholds, specifications, created_at) VALUES
('BMS', 'STORAGE', '{
  \"soc\": {\"type\": \"number\", \"unit\": \"%\", \"min\": 0, \"max\": 100},
  \"voltage\": {\"type\": \"number\", \"unit\": \"V\", \"min\": 0, \"max\": 1000},
  \"current\": {\"type\": \"number\", \"unit\": \"A\", \"min\": -500, \"max\": 500},
  \"temperature\": {\"type\": \"number\", \"unit\": \"°C\", \"min\": -20, \"max\": 60},
  \"power\": {\"type\": \"number\", \"unit\": \"kW\", \"min\": -1000, \"max\": 1000},
  \"energy_today\": {\"type\": \"number\", \"unit\": \"kWh\", \"min\": 0},
  \"cycle_count\": {\"type\": \"integer\", \"min\": 0},
  \"health\": {\"type\": \"number\", \"unit\": \"%\", \"min\": 0, \"max\": 100}
}', '{
  \"soc_low\": 20,
  \"soc_high\": 95,
  \"temperature_high\": 45,
  \"voltage_low\": 600,
  \"voltage_high\": 850
}', '{
  \"capacity_kwh\": 2400,
  \"max_power_kw\": 1000,
  \"efficiency\": 0.95,
  \"chemistry\": \"LiFePO4\"
}', NOW()),
('SOLAR_ARRAY', 'GENERATION', '{
  \"power\": {\"type\": \"number\", \"unit\": \"kW\", \"min\": 0, \"max\": 2000},
  \"voltage\": {\"type\": \"number\", \"unit\": \"V\", \"min\": 0, \"max\": 1000},
  \"current\": {\"type\": \"number\", \"unit\": \"A\", \"min\": 0, \"max\": 100},
  \"temperature\": {\"type\": \"number\", \"unit\": \"°C\", \"min\": -20, \"max\": 80},
  \"irradiance\": {\"type\": \"number\", \"unit\": \"W/m²\", \"min\": 0, \"max\": 1200},
  \"energy_today\": {\"type\": \"number\", \"unit\": \"kWh\", \"min\": 0},
  \"efficiency\": {\"type\": \"number\", \"unit\": \"%\", \"min\": 0, \"max\": 25}
}', '{
  \"temperature_high\": 65,
  \"power_low_threshold\": 50,
  \"efficiency_low\": 15
}', '{
  \"capacity_kw\": 1800,
  \"panel_count\": 48,
  \"panel_type\": \"Monocrystalline\",
  \"inverter_type\": \"String\"
}', NOW()),
('EV_CHARGER', 'CHARGING', '{
  \"power\": {\"type\": \"number\", \"unit\": \"kW\", \"min\": 0, \"max\": 350},
  \"voltage\": {\"type\": \"number\", \"unit\": \"V\", \"min\": 0, \"max\": 1000},
  \"current\": {\"type\": \"number\", \"unit\": \"A\", \"min\": 0, \"max\": 500},
  \"energy_delivered\": {\"type\": \"number\", \"unit\": \"kWh\", \"min\": 0},
  \"active_sessions\": {\"type\": \"integer\", \"min\": 0, \"max\": 10},
  \"temperature\": {\"type\": \"number\", \"unit\": \"°C\", \"min\": -20, \"max\": 60},
  \"status\": {\"type\": \"string\", \"enum\": [\"AVAILABLE\", \"CHARGING\", \"FAULTED\", \"OFFLINE\"]}
}', '{
  \"temperature_high\": 50,
  \"power_max\": 350,
  \"session_timeout\": 480
}', '{
  \"max_power_kw\": 350,
  \"connector_type\": \"CCS\",
  \"port_count\": 8,
  \"payment_methods\": [\"RFID\", \"APP\", \"CREDIT_CARD\"]
}', NOW());
"

DEVICE_TYPE_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DEVICE_DB_NAME -t -c "SELECT COUNT(*) FROM device_types;")
echo -e "${GREEN}  ✓ Created $DEVICE_TYPE_COUNT device types${NC}"

# Create 50 sites (first 10 will have MQTT devices)
echo "  • Creating 50 sites..."

# Create the first 10 MQTT-enabled sites
execute_sql $DEVICE_DB_NAME "
INSERT INTO sites (name, description, location_lat, location_lng, capacity_mw, status, timezone, address, contact_person, contact_email, contact_phone, created_at, updated_at) VALUES
('New York Corporate HQ', 'Primary corporate headquarters with comprehensive EMS', 40.7128, -74.0060, 5.2, 'ACTIVE', 'America/New_York', '123 Energy Plaza, New York, NY 10001', 'John Smith', 'john.smith@ecogrid.com', '+1-212-555-0001', NOW(), NOW()),
('Los Angeles Solar Farm', 'Large-scale solar installation with battery storage', 34.0522, -118.2437, 8.5, 'ACTIVE', 'America/Los_Angeles', '456 Solar Avenue, Los Angeles, CA 90210', 'Maria Garcia', 'maria.garcia@ecogrid.com', '+1-323-555-0002', NOW(), NOW()),
('Chicago Industrial Complex', 'Manufacturing facility with microgrid', 41.8781, -87.6298, 3.8, 'ACTIVE', 'America/Chicago', '789 Industrial Blvd, Chicago, IL 60601', 'Robert Johnson', 'robert.johnson@ecogrid.com', '+1-312-555-0003', NOW(), NOW()),
('Houston Energy Hub', 'Major energy storage and distribution center', 29.7604, -95.3698, 12.3, 'ACTIVE', 'America/Chicago', '321 Energy Way, Houston, TX 77001', 'Lisa Chen', 'lisa.chen@ecogrid.com', '+1-713-555-0004', NOW(), NOW()),
('Phoenix Solar Park', 'Desert solar installation with EV charging', 33.4484, -112.0740, 6.7, 'ACTIVE', 'America/Phoenix', '654 Desert Drive, Phoenix, AZ 85001', 'David Miller', 'david.miller@ecogrid.com', '+1-602-555-0005', NOW(), NOW()),
('Miami Beach Resort', 'Luxury resort with sustainable energy systems', 25.7617, -80.1918, 2.1, 'ACTIVE', 'America/New_York', '987 Ocean Drive, Miami Beach, FL 33139', 'Sofia Rodriguez', 'sofia.rodriguez@ecogrid.com', '+1-305-555-0006', NOW(), NOW()),
('Seattle Tech Campus', 'Technology campus with advanced energy management', 47.6062, -122.3321, 4.5, 'ACTIVE', 'America/Los_Angeles', '147 Tech Boulevard, Seattle, WA 98101', 'Kevin Park', 'kevin.park@ecogrid.com', '+1-206-555-0007', NOW(), NOW()),
('Denver Mountain Facility', 'High-altitude renewable energy testing facility', 39.7392, -104.9903, 3.2, 'ACTIVE', 'America/Denver', '258 Mountain View Road, Denver, CO 80201', 'Amanda White', 'amanda.white@ecogrid.com', '+1-303-555-0008', NOW(), NOW()),
('Atlanta Distribution Center', 'Major logistics hub with comprehensive EMS', 33.7490, -84.3880, 7.8, 'ACTIVE', 'America/New_York', '369 Logistics Lane, Atlanta, GA 30301', 'Michael Brown', 'michael.brown@ecogrid.com', '+1-404-555-0009', NOW(), NOW()),
('Portland Green Campus', 'Sustainable business park with microgrid', 45.5152, -122.6784, 5.9, 'ACTIVE', 'America/Los_Angeles', '741 Green Street, Portland, OR 97201', 'Jennifer Davis', 'jennifer.davis@ecogrid.com', '+1-503-555-0010', NOW(), NOW());
"

# Create 40 additional monitoring-only sites
execute_sql $DEVICE_DB_NAME "
INSERT INTO sites (name, description, location_lat, location_lng, capacity_mw, status, timezone, address, contact_person, contact_email, contact_phone, created_at, updated_at) VALUES
('Boston Medical Center', 'Hospital complex with backup power systems', 42.3601, -71.0589, 4.2, 'ACTIVE', 'America/New_York', '852 Medical Drive, Boston, MA 02101', 'Dr. Sarah Wilson', 'sarah.wilson@ecogrid.com', '+1-617-555-0011', NOW(), NOW()),
('San Francisco Tech Hub', 'Technology incubator with renewable energy', 37.7749, -122.4194, 3.1, 'ACTIVE', 'America/Los_Angeles', '963 Innovation Way, San Francisco, CA 94101', 'Alex Thompson', 'alex.thompson@ecogrid.com', '+1-415-555-0012', NOW(), NOW()),
('Las Vegas Entertainment Complex', 'Casino and hotel with large energy demand', 36.1699, -115.1398, 8.9, 'ACTIVE', 'America/Los_Angeles', '159 Strip Boulevard, Las Vegas, NV 89101', 'Tony Martinez', 'tony.martinez@ecogrid.com', '+1-702-555-0013', NOW(), NOW()),
('Detroit Manufacturing Plant', 'Automotive manufacturing with energy optimization', 42.3314, -83.0458, 6.4, 'ACTIVE', 'America/New_York', '357 Auto Lane, Detroit, MI 48201', 'Linda Jackson', 'linda.jackson@ecogrid.com', '+1-313-555-0014', NOW(), NOW()),
('Nashville Music District', 'Entertainment district with smart grid', 36.1627, -86.7816, 2.8, 'ACTIVE', 'America/Chicago', '456 Music Row, Nashville, TN 37201', 'Country Smith', 'country.smith@ecogrid.com', '+1-615-555-0015', NOW(), NOW());"

# Continue with more sites...
for i in {16..50}; do
    site_name="Test Site $i"
    lat=$(echo "scale=4; 25 + ($RANDOM % 20)" | bc)
    lng=$(echo "scale=4; -125 + ($RANDOM % 50)" | bc)
    capacity=$(echo "scale=1; 1 + ($RANDOM % 15)" | bc)
    
    execute_sql $DEVICE_DB_NAME "
    INSERT INTO sites (name, description, location_lat, location_lng, capacity_mw, status, timezone, address, contact_person, contact_email, contact_phone, created_at, updated_at) VALUES
    ('$site_name', 'Automated test site for monitoring', $lat, $lng, $capacity, 'ACTIVE', 'America/New_York', '${i}00 Test Avenue, Test City, TC ${i}001', 'Test Manager $i', 'test${i}@ecogrid.com', '+1-555-555-$(printf "%04d" $i)', NOW(), NOW());"
done

SITE_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DEVICE_DB_NAME -t -c "SELECT COUNT(*) FROM sites;")
echo -e "${GREEN}  ✓ Created $SITE_COUNT sites${NC}"

# Create devices for the first 10 sites (4 devices each: 2 BMS, 1 Solar, 1 EV)
echo "  • Creating devices for MQTT-enabled sites..."

# Get the first 10 site IDs
site_ids=($(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DEVICE_DB_NAME -t -c "SELECT id FROM sites ORDER BY id LIMIT 10;" | tr -d ' '))

# Get device type IDs
bms_type_id=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DEVICE_DB_NAME -t -c "SELECT id FROM device_types WHERE name = 'BMS';" | tr -d ' ')
solar_type_id=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DEVICE_DB_NAME -t -c "SELECT id FROM device_types WHERE name = 'SOLAR_ARRAY';" | tr -d ' ')
ev_type_id=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DEVICE_DB_NAME -t -c "SELECT id FROM device_types WHERE name = 'EV_CHARGER';" | tr -d ' ')

# Device creation for first 10 sites
for i in {0..9}; do
    site_id=${site_ids[$i]}
    logical_site_id=$((i + 1))
    
    # Calculate base IP for this site
    base_ip="192.168.$logical_site_id"
    
    # Create devices for this site
    execute_sql $DEVICE_DB_NAME "
    INSERT INTO devices (serial_number, name, description, device_type, device_type_id, model, manufacturer, status, mqtt_topic, ip_address, mac_address, installation_date, site_id, created_at, updated_at) VALUES
    ('BMS-SITE${logical_site_id}-001', 'Battery Management System 1', 'Primary BMS for site $logical_site_id', 'BATTERY_STORAGE', $bms_type_id, 'PowerMax Pro 2400', 'Tesla Energy', 'ONLINE', 'ecogrid/site${logical_site_id}/bms/001', '${base_ip}.101', '$(printf "00:1B:44:%02d:%02d:B1" $logical_site_id $logical_site_id)', '2024-01-15', $site_id, NOW(), NOW()),
    ('BMS-SITE${logical_site_id}-002', 'Battery Management System 2', 'Secondary BMS for site $logical_site_id', 'BATTERY_STORAGE', $bms_type_id, 'PowerMax Pro 2400', 'Tesla Energy', 'ONLINE', 'ecogrid/site${logical_site_id}/bms/002', '${base_ip}.102', '$(printf "00:1B:44:%02d:%02d:B2" $logical_site_id $logical_site_id)', '2024-01-15', $site_id, NOW(), NOW()),
    ('SOLAR-SITE${logical_site_id}-001', 'Solar Array System', 'Primary solar installation for site $logical_site_id', 'SOLAR_INVERTER', $solar_type_id, 'SolarEdge SE25K', 'SolarEdge', 'ONLINE', 'ecogrid/site${logical_site_id}/solar/001', '${base_ip}.201', '$(printf "00:1B:44:%02d:%02d:S1" $logical_site_id $logical_site_id)', '2024-01-10', $site_id, NOW(), NOW()),
    ('EV-SITE${logical_site_id}-001', 'EV Charging Station', 'EV charging facility for site $logical_site_id', 'EV_CHARGER', $ev_type_id, 'Supercharger V3', 'Tesla', 'ONLINE', 'ecogrid/site${logical_site_id}/ev/001', '${base_ip}.301', '$(printf "00:1B:44:%02d:%02d:E1" $logical_site_id $logical_site_id)', '2024-01-20', $site_id, NOW(), NOW());"
done

# Add some device configurations
echo "  • Adding device configurations..."
execute_sql $DEVICE_DB_NAME "
INSERT INTO device_configuration (device_id, config_key, config_value) SELECT 
    d.id, 'max_soc', '95' FROM devices d WHERE d.device_type_id = $bms_type_id AND d.id <= 20;
INSERT INTO device_configuration (device_id, config_key, config_value) SELECT 
    d.id, 'min_soc', '20' FROM devices d WHERE d.device_type_id = $bms_type_id AND d.id <= 20;
INSERT INTO device_configuration (device_id, config_key, config_value) SELECT 
    d.id, 'max_power_kw', '1000' FROM devices d WHERE d.device_type_id = $bms_type_id AND d.id <= 20;
"

# Get final counts
DEVICE_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DEVICE_DB_NAME -t -c "SELECT COUNT(*) FROM devices;")
MQTT_DEVICE_COUNT=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DEVICE_DB_NAME -t -c "SELECT COUNT(*) FROM devices WHERE mqtt_topic IS NOT NULL;")

echo -e "${GREEN}  ✓ Created $DEVICE_COUNT devices${NC}"
echo -e "${GREEN}  ✓ $MQTT_DEVICE_COUNT devices have MQTT topics${NC}"

# =============================================================================
# SUMMARY
# =============================================================================

echo ""
echo -e "${GREEN}🎉 Database seeding completed successfully!${NC}"
echo "========================================"
echo -e "${BLUE}Summary:${NC}"
echo "  • Users created: $(echo $USER_COUNT | xargs)"
echo "  • Sites created: $(echo $SITE_COUNT | xargs)"
echo "  • Device types: $(echo $DEVICE_TYPE_COUNT | xargs)"
echo "  • Total devices: $(echo $DEVICE_COUNT | xargs)"
echo "  • MQTT-enabled devices: $(echo $MQTT_DEVICE_COUNT | xargs)"
echo ""
echo -e "${YELLOW}Test User Credentials (password: password123):${NC}"
echo "  • admin@ecogrid.com (ADMIN)"
echo "  • test@ecogrid.com (OPERATOR)"
echo "  • operator1@ecogrid.com (OPERATOR)"
echo "  • viewer1@ecogrid.com (VIEWER)"
echo ""
echo -e "${YELLOW}MQTT Test Sites (Sites 1-10):${NC}"
echo "  • Each site has 4 devices: 2 BMS + 1 Solar + 1 EV Charger"
echo "  • MQTT Topics: ecogrid/site{1-10}/{bms|solar|ev}/{001|002}"
echo "  • IP Range: 192.168.{1-10}.{101-301}"
echo ""
echo -e "${BLUE}Next Steps:${NC}"
echo "  1. Start the backend services"
echo "  2. Send MQTT telemetry to sites 1-10"
echo "  3. Verify WebSocket data flow to frontend"
echo ""
echo -e "${GREEN}Ready for end-to-end testing! 🚀${NC}"