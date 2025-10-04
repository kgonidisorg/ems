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

