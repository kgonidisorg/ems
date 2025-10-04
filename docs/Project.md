# EcoGrid Energy Management System (EMS) - Monorepo Project Plan

## Project Overview

EcoGrid EMS is a comprehensive energy management system designed to monitor, control, and optimize energy flow across distributed renewable energy resources. This project will evolve from the current Next.js frontend into a full-stack monorepo including backend services, DevOps infrastructure, and enhanced frontend capabilities.

## Current State Analysis

### Existing Frontend (Next.js 15)
The current implementation includes:

#### ✅ Implemented Features
- **Dashboard with Real-time KPIs**: Total sites (342), capacity (2450 MW), carbon saved, grid revenue
- **Interactive Map View**: Mapbox integration showing site locations with popups
- **Navigation Structure**: Multi-page app with Home, Network, EMS, Analytics, API, Compute sections
- **Analytics Pages**: Time-series graphs, CO2 charts, financial dashboards, report scheduling
- **Modern UI Stack**: Tailwind CSS, TypeScript, React Icons, Recharts
- **Responsive Design**: Mobile-friendly navigation and layouts
- **Component Architecture**: Modular components (MapView, Topbar, KPIStrip, etc.)

#### 🔄 Simulated Features (Need Backend Integration)
- Real-time data updates (currently using `setInterval` with random data)
- Site generation and management
- Energy flow calculations
- Analytics and reporting data

#### ❌ Missing Frontend Features
- User authentication and authorization
- Real WebSocket connections for live data
- Device control interfaces
- Advanced filtering and search capabilities
- Dark mode toggle
- User preferences and settings
- Alert and notification system
- Data export functionality
- Mobile app considerations

## Monorepo Structure Plan

```
ems/
├── frontend/                    # Next.js 15 application
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── ...existing frontend files
├── backend/                     # Spring Boot microservices
│   ├── api-gateway/            # Spring Cloud Gateway
│   ├── auth-service/           # JWT authentication service
│   ├── device-service/         # Device management
│   ├── analytics-service/      # Data processing & analytics
│   ├── notification-service/   # Alerts & notifications
│   ├── shared/                 # Common utilities and DTOs
│   └── docker-compose.services.yml
├── infrastructure/             # DevOps and Infrastructure
│   ├── docker/                # Dockerfiles for all services
│   ├── kubernetes/            # K8s manifests and Helm charts
│   ├── terraform/             # Infrastructure as Code
│   ├── scripts/               # Deployment and utility scripts
│   └── monitoring/            # Prometheus, Grafana configs
├── shared/                     # Cross-platform shared code
│   ├── types/                 # TypeScript type definitions
│   ├── constants/             # Shared constants
│   └── utils/                 # Common utilities
├── docs/                       # Documentation
├── .github/                    # CI/CD workflows
├── docker-compose.yml          # Full stack development
├── package.json               # Root package.json
└── README.md
```

## Backend Architecture Plan

### Communication Architecture Decision

The EMS system uses a **hybrid communication approach** optimized for different layers:

#### IoT Device Layer (MQTT)
```
Solar Inverters, Battery Systems, EV Chargers
              ↓ (MQTT over TCP/TLS)
         MQTT Broker (Mosquitto/HiveMQ)
              ↓ (Kafka Connect)
           Kafka Message Queue
```

**Why MQTT for devices:**
- **QoS Levels**: Ensure critical energy data delivery (QoS 1/2)
- **Topic Structure**: `sites/{siteId}/devices/{deviceId}/telemetry/{metric}`
- **Retained Messages**: Last-known values automatically available
- **Lightweight**: Minimal bandwidth for battery-powered devices
- **Last Will Testament**: Automatic device offline detection
- **Security**: TLS encryption + client certificates

#### Frontend Layer (WebSockets)
```
React Dashboard
       ↕ (WebSocket over WSS)
   WebSocket Gateway Service
       ↕ (Internal APIs)
   Backend Microservices
```

**Why WebSockets for frontend:**
- **Bidirectional**: Stream telemetry data AND send device commands
- **Low Latency**: Real-time chart updates without polling
- **Single Connection**: Efficient resource usage per user
- **Command Flow**: Immediate acknowledgment of device control actions

#### Command Flow Example
```
User clicks "Turn Off Inverter" → WebSocket → Command Service → MQTT Publish → Device
Device Acknowledges Command → MQTT → Kafka → WebSocket → UI Update
```

### Technology Stack
- **Framework**: Spring Boot 3.x with Spring Data JPA
- **API**: REST + GraphQL endpoints
- **Database**: PostgreSQL (primary), Redis (caching)
- **Message Queue**: Apache Kafka for real-time data streaming
- **IoT Protocol**: MQTT for device communication (with QoS levels)
- **Real-time Frontend**: WebSockets for bidirectional dashboard communication
- **Security**: Spring Security with JWT tokens
- **Documentation**: OpenAPI 3.0 (Swagger)

### Microservices Design

#### 1. API Gateway Service
- **Purpose**: Single entry point, routing, rate limiting
- **Tech**: Spring Cloud Gateway
- **Features**: 
  - Route management
  - Authentication middleware
  - CORS handling
  - Request/response logging

#### 2. Authentication Service
- **Purpose**: User management and JWT token handling
- **Features**:
  - User registration/login
  - Role-based access control (Admin, Operator, Viewer)
  - Password reset functionality
  - OAuth2 integration (future)
  - Session management

#### 3. Device Management Service
- **Purpose**: IoT device registry and control
- **Features**:
  - Device registration and configuration
  - Device status monitoring
  - Command dispatch to devices
  - Device group management
  - Firmware update coordination

#### 4. Analytics Service
- **Purpose**: Data processing and analytics
- **Features**:
  - Real-time data aggregation
  - Historical data analysis
  - Anomaly detection algorithms
  - Report generation
  - Predictive analytics
  - Carbon footprint calculations

#### 5. Notification Service
- **Purpose**: Alert and notification management
- **Features**:
  - Real-time alerts via WebSocket to frontend
  - MQTT-based device alert monitoring
  - Email notifications
  - SMS integration (future)
  - Notification preferences
  - Alert rule configuration
  - Integration with MQTT Last Will Testament for device offline alerts

### Database Schema Plan

#### Core Entities
```sql
-- Users and Authentication
users (id, username, email, password_hash, role, created_at, updated_at)
user_sessions (id, user_id, token_hash, expires_at)

-- Sites and Devices
sites (id, name, location_lat, location_lng, capacity_mw, status, created_at)
devices (id, site_id, type, model, status, last_seen, configuration)
device_types (id, name, category, specifications)

-- Energy Data
energy_readings (id, device_id, timestamp, power_kw, energy_kwh, voltage, current)
energy_aggregates (id, site_id, date, total_generation, total_consumption, grid_export)

-- Analytics and Reports
alerts (id, device_id, type, severity, message, acknowledged, created_at)
reports (id, user_id, type, parameters, generated_at, file_path)
```

### Real-time Data Flow Architecture

```
IoT Devices → MQTT Broker → Kafka → Analytics Service → WebSocket → Frontend
     ↑                        ↓              ↓              ↑
     └── Device Commands ← REST API ← WebSocket ──────────────┘
                              ↓
                         PostgreSQL ← Redis Cache
```

#### Protocol Selection Rationale

**MQTT for IoT Communication:**
- **Device → Backend**: MQTT is the optimal choice for IoT device telemetry
  - Quality of Service (QoS) levels ensure critical data delivery
  - Topic hierarchy: `sites/{siteId}/devices/{deviceId}/{metric}`
  - Retained messages provide last-known device state
  - Lightweight protocol perfect for energy monitoring devices
  - Built-in Last Will Testament for device offline detection

**WebSocket for Frontend Communication:**
- **Backend ↔ Frontend**: WebSockets provide optimal user experience
  - Bidirectional: Real-time data streaming + device control commands
  - Low latency for live dashboard updates
  - Single persistent connection reduces overhead
  - Perfect for interactive device control and acknowledgments

**Alternative Considered: MQTT + Server-Sent Events (SSE)**
- **Pros**: Simpler HTTP-based SSE, better firewall compatibility
- **Cons**: Unidirectional SSE requires separate REST calls for commands
- **Decision**: WebSocket's bidirectional nature better suits interactive energy management

## DevOps and Infrastructure Plan

### Containerization Strategy
- **Frontend**: Nginx-based container for static Next.js build
- **Backend Services**: Individual Docker containers per microservice
- **Databases**: Containerized PostgreSQL, Redis
- **Message Queue**: Kafka + Zookeeper containers
- **MQTT Broker**: Eclipse Mosquitto or HiveMQ container
- **Monitoring**: Prometheus, Grafana, Jaeger for tracing

### CI/CD Pipeline (GitHub Actions)
```yaml
# Planned workflow stages:
1. Code Quality: ESLint, SonarQube, security scanning
2. Testing: Unit tests, integration tests, e2e tests
3. Build: Docker image creation for all services
4. Deploy: 
   - Development: Auto-deploy to dev environment
   - Staging: Manual approval for staging deployment
   - Production: Manual approval with blue-green deployment
```

### Kubernetes Deployment
- **Development**: minikube or kind for local development
- **Staging/Production**: Cloud provider (AWS EKS, GCP GKE, or Azure AKS)
- **Helm Charts**: For templated deployments
- **Service Mesh**: Istio for advanced traffic management (future)

### Infrastructure as Code (Terraform)
```
modules/
├── networking/      # VPC, subnets, security groups
├── kubernetes/      # EKS cluster, node groups
├── database/        # RDS PostgreSQL, ElastiCache Redis
├── monitoring/      # CloudWatch, Prometheus setup
└── security/        # IAM roles, secrets management
```

## Development Phases

### Phase 1: Backend Foundation (Weeks 1-4)
1. **Setup monorepo structure**
2. **Implement Authentication Service**
   - User registration/login APIs
   - JWT token management
   - Role-based access control
3. **Create API Gateway**
   - Route configuration
   - Authentication middleware
4. **Database setup and migrations**
5. **Basic Docker containerization**

### Phase 2: Core Services (Weeks 5-8)
1. **Device Management Service**
   - Device CRUD operations
   - Site management
   - Basic device status tracking
2. **Analytics Service Foundation**
   - Data ingestion endpoints
   - Basic aggregation queries
   - Historical data storage
3. **WebSocket implementation for real-time updates**

### Phase 3: Frontend Integration (Weeks 9-12)
1. **Authentication flow integration**
   - Login/logout functionality
   - Protected routes
   - User role handling
2. **Real-time data integration**
   - Replace mock data with API calls
   - WebSocket connections
   - Error handling and loading states
3. **Enhanced UI features**
   - User settings
   - Dark mode
   - Advanced filtering

### Phase 4: Advanced Features (Weeks 13-16)
1. **Complete Analytics Service**
   - Anomaly detection
   - Report generation
   - Predictive analytics
2. **Notification System**
   - Real-time alerts
   - Email notifications
   - Alert configuration
3. **Device Control Features**
   - Command dispatch
   - Bulk operations
   - Scheduling

### Phase 5: DevOps and Production (Weeks 17-20)
1. **Complete CI/CD pipeline**
2. **Kubernetes deployment**
3. **Infrastructure as Code**
4. **Monitoring and logging**
5. **Performance optimization**
6. **Security hardening**

## Enhanced Frontend Features Plan

### Authentication & User Management
- Login/register forms with validation
- Password reset flow
- User profile management
- Role-based UI components

### Real-time Dashboard Enhancements
- WebSocket connection management
- Live data streaming indicators
- Connection status monitoring
- Offline mode handling

### Advanced Analytics UI
- Interactive time-range selectors
- Drill-down capabilities for site-specific data
- Export functionality (CSV, PDF reports)
- Custom dashboard creation
- Alert rule configuration interface

### Device Management Interface
- Device inventory table with sorting/filtering
- Device detail modal with real-time status
- Bulk device operations
- Device configuration forms
- Command history tracking

### Mobile Responsiveness
- Progressive Web App (PWA) capabilities
- Touch-optimized controls
- Offline data caching
- Push notifications

## Technology Integration Points

### Frontend → Backend Integration
- **Authentication**: JWT tokens in HTTP headers
- **Real-time Data**: WebSocket connections to notification service
- **API Calls**: RESTful endpoints for CRUD operations
- **GraphQL**: Complex queries for analytics data
- **File Uploads**: Device configuration files, report exports

### Backend → Infrastructure Integration
- **Message Queues**: Kafka for decoupled service communication
- **Caching**: Redis for frequently accessed data
- **Monitoring**: Metrics collection and health checks
- **Secrets Management**: Environment-based configuration

## Success Metrics

### Technical Metrics
- **Performance**: < 100ms API response times, < 1s page load times
- **Reliability**: 99.9% uptime, < 5min recovery time
- **Scalability**: Support for 10,000+ devices, 1000+ concurrent users
- **Security**: Zero critical vulnerabilities, SOC2 compliance ready

### Business Metrics
- **User Experience**: < 3 clicks to key actions
- **Data Accuracy**: Real-time data within 5 seconds of generation
- **Operational Efficiency**: 50% reduction in manual monitoring tasks
- **Cost Optimization**: Automated scaling reducing infrastructure costs by 30%

## Risk Assessment and Mitigation

### Technical Risks
1. **Data Volume**: High-frequency IoT data could overwhelm system
   - *Mitigation*: Implement data aggregation and archival strategies
2. **Real-time Performance**: WebSocket scalability challenges
   - *Mitigation*: Load balancing and connection pooling
3. **Security**: API vulnerabilities and data breaches
   - *Mitigation*: Regular security audits, encryption, and access controls

### Business Risks
1. **Scope Creep**: Feature additions beyond initial plan
   - *Mitigation*: Strict change control process and phase gates
2. **Timeline Delays**: Complex integrations taking longer than expected
   - *Mitigation*: Incremental delivery and MVP approach

## Next Steps

1. **Immediate Actions**:
   - Setup monorepo structure
   - Initialize backend services with Spring Boot
   - Configure development environment with Docker Compose

2. **Week 1 Priorities**:
   - Move existing frontend code to `/frontend` directory
   - Create initial Spring Boot authentication service
   - Setup PostgreSQL and Redis containers
   - Implement basic CI/CD workflow

3. **Stakeholder Review**:
   - Architecture approval
   - Technology stack confirmation
   - Resource allocation and timeline agreement

---

*This document serves as the living blueprint for the EcoGrid EMS project. It will be updated as requirements evolve and implementation progresses.*