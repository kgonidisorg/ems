"""
EMS Database Seeding Script (Python)

Seeds both ems_auth and ems_devices databases with test data for EMS development.
Deletes all existing data and inserts users, device types, sites, and devices.

Requires: psycopg2
"""

import os
import psycopg2
from datetime import datetime


def get_conn(db_name):
    return psycopg2.connect(
        dbname=db_name,
        user=os.getenv("PGUSER", "ems_user"),
        password=os.getenv("PGPASSWORD", "ems_password"),
        host=os.getenv("PGHOST", "localhost"),
        port=os.getenv("PGPORT", "5433"),
    )


def execute_sql(conn, sql, params=None):
    with conn:
        with conn.cursor() as cur:
            cur.execute(sql, params)


def seed_auth_db():
    print("Seeding auth service database...")
    conn = get_conn(os.getenv("AUTH_DB_NAME", "ems_auth"))
    # Clear users
    execute_sql(conn, "DELETE FROM users;")
    # Insert test users
    execute_sql(
        conn,
        """
    INSERT INTO users (email, password_hash, first_name, last_name, role, account_enabled, account_locked, credentials_expired, created_at, updated_at) VALUES
    ('admin@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Admin', 'User', 'ADMIN', true, false, false, NOW(), NOW()),
    ('superadmin@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Super', 'Admin', 'ADMIN', true, false, false, NOW(), NOW()),
    ('operator1@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'John', 'Smith', 'OPERATOR', true, false, false, NOW(), NOW()),
    ('operator2@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Sarah', 'Johnson', 'OPERATOR', true, false, false, NOW(), NOW()),
    ('operator3@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Mike', 'Davis', 'OPERATOR', true, false, false, NOW(), NOW()),
    ('viewer1@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Emily', 'Brown', 'VIEWER', true, false, false, NOW(), NOW()),
    ('viewer2@ecogrid.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.MQJ4B5rFLVhZ4k0jLvYFyXk2P8m/aK', 'Tom', 'Wilson', 'VIEWER', true, false, false, NOW(), NOW()),
    ('test@ecogrid.com', '$2a$10$A8Qo5dYBBDB75Kqupas2C.8WQWQLu6wQPtjX4Va./tTs5rL4EmGjm', 'Test', 'User', 'OPERATOR', true, false, false, NOW(), NOW());
    """,
    )
    print("✓ Auth users seeded.")
    conn.close()


def seed_device_db():
    print("Seeding device service database...")
    conn = get_conn(os.getenv("DEVICE_DB_NAME", "ems_devices"))
    # Clear data
    for table in [
        "device_telemetry",
        "device_metadata",
        "device_configuration",
        "devices",
        "sites",
        "device_types",
    ]:
        execute_sql(conn, f"DELETE FROM {table};")
    # Insert device types
    execute_sql(
        conn,
        """
    INSERT INTO device_types (name, category, telemetry_schema, alert_thresholds, specifications, created_at) VALUES
    ('BMS', 'STORAGE', '{"soc": {"type": "number", "unit": "%", "min": 0, "max": 100}}', '{"soc_low": 20}', '{"capacity_kwh": 2400}', NOW()),
    ('SOLAR_ARRAY', 'GENERATION', '{"power": {"type": "number", "unit": "kW", "min": 0, "max": 2000}}', '{"power_low_threshold": 50}', '{"capacity_kw": 1800}', NOW()),
    ('EV_CHARGER', 'CHARGING', '{"power": {"type": "number", "unit": "kW", "min": 0, "max": 350}}', '{"power_max": 350}', '{"max_power_kw": 350}', NOW());
    """,
    )

    # Insert sites
    execute_sql(
        conn,
        """INSERT INTO sites (name, description, location_lat, location_lng, capacity_mw, status, timezone, address, contact_person, contact_email, contact_phone, created_at, updated_at) VALUES
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
""",
    )
    print("✓ Sites seeded.")
    # Insert devices for first 10 sites
    for site_id in range(1, 11):
        for dev_num, dev_type, dev_name, dev_type_id in [
            (1, "BMS", "Battery Management System 1", "BMS"),
            (2, "BMS", "Battery Management System 2", "BMS"),
            (1, "SOLAR", "Solar Array System", "SOLAR_ARRAY"),
            (1, "EV", "EV Charging Station", "EV_CHARGER"),
        ]:
            execute_sql(
                conn,
                """
            INSERT INTO devices (serial_number, name, description, model, manufacturer, status, mqtt_topic, ip_address, mac_address, installation_date, site_id, created_at, updated_at, device_type_id)
            VALUES (%s, %s, %s, %s, %s, 'ONLINE', %s, %s, %s, %s, %s, NOW(), NOW(), (SELECT id FROM device_types WHERE name = %s));
            """,
                (
                    f"{dev_type}-SITE{site_id}-{dev_num:03d}",
                    dev_name,
                    f"{dev_name} for site {site_id}",
                    "ModelX",
                    "ManufacturerY",
                    f"ecogrid/site{site_id}/{dev_type.lower()}/{dev_num:03d}",
                    f"192.168.{site_id}.{100+dev_num}",
                    f"00:1B:44:{site_id:02d}:{dev_num:02d}:A1",
                    "2024-01-01",
                    site_id,
                    dev_type_id,
                ),
            )
    print("✓ Devices seeded.")
    conn.close()


def main():
    print("\n🚀 EMS Database Seeding (Python)\n==============================")
    seed_auth_db()
    seed_device_db()
    print("\n🎉 Database seeding completed successfully!\n")


if __name__ == "__main__":
    main()
