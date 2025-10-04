# EcoGrid Energy Management System (EMS) - Monorepo Project Plan

## Project Overview

EcoGrid EMS is a comprehensive energy management system designed to monitor, control, and optimize energy flow across distributed renewable energy resources. This project will evolve from the current Next.js frontend into a full-stack monorepo including backend services, DevOps infrastructure, and enhanced frontend capabilities.

## 📈 Progress Tracking

### ✅ Completed (October 2025)

#### Monorepo Foundation Setup
- **✅ Repository Structure**: Successfully restructured project into monorepo format
  - Moved Next.js frontend to `/frontend` directory
  - Created `/backend`, `/infrastructure`, `/shared`, and `/docs` directories
  - Maintained full git history for moved files (detected as renames)

- **✅ Frontend Verification**: Confirmed frontend functionality in new structure
  - Dependencies properly installed and managed
  - Build process working correctly (Next.js 15 optimized production build)
  - All components and pages functional

- **✅ CI/CD Pipeline Updates**: Modified GitHub Actions workflow
  - Updated Docker build context to `./frontend`
  - Configured to use `./infrastructure/docker/Dockerfile.frontend`
  - Multi-architecture builds (AMD64/ARM64) properly configured

- **✅ Enhanced Development Environment**:
  - Comprehensive `.gitignore` for monorepo structure
  - Covers frontend (Node.js/Next.js), backend (Java/Spring Boot), infrastructure (Terraform/Kubernetes)
  - Includes security patterns, IDE files, and OS-specific exclusions

#### Backend Services Foundation
- **✅ Spring Boot Microservices Structure**: Complete 5-service architecture created
  - **API Gateway** (port 8080): Spring Cloud Gateway with route configuration and JWT authentication
  - **Auth Service** (port 8081): User authentication with JWT, PostgreSQL, and Redis integration
  - **Device Service** (port 8082): IoT device management with MQTT broker integration
  - **Analytics Service** (port 8083): Data processing service with PostgreSQL connection
  - **Notification Service** (port 8084): Alert and notification management system

- **✅ Shared Library**: Common DTOs and utilities
  - AuthRequest, AuthResponse, UserInfo DTOs with validation
  - ApiConstants for centralized endpoint definitions
  - Maven configuration for cross-service dependencies

- **✅ Service Verification**: All services successfully tested
  - Maven compilation verified for all microservices
  - Service startup and runtime testing completed
  - Package declarations and dependencies properly configured

#### Database and Infrastructure Setup
- **✅ Database Configuration**: Multi-database Docker setup
  - PostgreSQL primary database with initialization scripts
  - Redis for caching and session management
  - MQTT broker (Mosquitto) for IoT device communication
  - Complete Docker Compose configuration for development environment

- **✅ Docker Configuration**: Complete containerization strategy
  - Individual Dockerfiles for each backend service
  - Production-optimized frontend Dockerfile with Nginx serving
  - Multi-stage build processes for efficient image sizes
  - Development docker-compose.yml with service dependencies

#### Development Tooling and Workspace
- **✅ VS Code Workspace Configuration**: Complete IDE setup for monorepo
  - Multi-folder workspace configuration (ems.code-workspace)
  - Comprehensive settings.json with Java, Maven, TypeScript configurations
  - Debug launch configurations for all services
  - Recommended extensions for full-stack development
  - IntelliSense and code completion properly configured

- **✅ Maven Build System**: Complete project structure
  - Parent POM configuration with dependency management
  - Individual service POMs with Spring Boot starters
  - Shared library packaging and distribution
  - All services compile and package successfully

### 🎯 Next Priority Items

#### Backend Business Logic Implementation
- **⏳ Authentication Service APIs**: Implement JWT authentication endpoints
  - User registration and login endpoints
  - Password reset functionality
  - Role-based access control implementation
  - Session management with Redis

- **⏳ API Gateway Routing**: Complete gateway configuration
  - Service discovery integration
  - Rate limiting and security policies
  - Request/response transformation

- **⏳ Device Management APIs**: Core device functionality
  - Device registration and CRUD operations
  - MQTT message processing and device communication
  - Site management and device grouping

#### Advanced Infrastructure
- **⏳ Message Queue Integration**: Kafka setup for real-time data streaming
  - Event-driven architecture between services
  - Real-time analytics data processing
  - Device command and telemetry routing

- **⏳ Testing Framework**: Automated testing across services
  - Unit tests for each microservice
  - Integration tests for service communication
  - End-to-end testing with Testcontainers

#### Frontend-Backend Integration
- **⏳ API Integration**: Replace mock data with real backend calls
- **⏳ WebSocket Implementation**: Real-time data streaming
- **⏳ Authentication Flow**: Login/logout with JWT tokens

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

## Monorepo Structure Status

### ✅ Current Implementation (October 2025)
```
ems/
├── frontend/                    # ✅ Next.js 15 application (COMPLETED)
│   ├── src/                    # ✅ All React components and pages
│   │   ├── app/               # ✅ Next.js 15 app router structure
│   │   └── components/        # ✅ Reusable UI components
│   ├── public/                # ✅ Static assets
│   ├── package.json           # ✅ Frontend dependencies
│   ├── next.config.ts         # ✅ Next.js configuration
│   └── tsconfig.json          # ✅ TypeScript configuration
├── backend/                     # ✅ Spring Boot microservices (STRUCTURE COMPLETE)
│   ├── api-gateway/            # ✅ Spring Cloud Gateway (Port 8080)
│   ├── auth-service/           # ✅ JWT authentication service (Port 8081)
│   ├── device-service/         # ✅ Device management service (Port 8082)
│   ├── analytics-service/      # ✅ Data processing service (Port 8083)
│   ├── notification-service/   # ✅ Alert management service (Port 8084)
│   ├── shared/                 # ✅ Common DTOs and utilities
│   └── pom.xml                # ✅ Parent Maven configuration
├── infrastructure/             # ✅ DevOps infrastructure (EXPANDED)
│   ├── docker/               # ✅ Complete container configurations
│   │   ├── Dockerfile.frontend # ✅ Production-ready frontend container
│   │   ├── Dockerfile.api-gateway # ✅ API Gateway container
│   │   ├── Dockerfile.auth-service # ✅ Auth service container
│   │   ├── Dockerfile.device-service # ✅ Device service container
│   │   ├── Dockerfile.analytics-service # ✅ Analytics service container
│   │   └── Dockerfile.notification-service # ✅ Notification service container
│   ├── kubernetes/           # ⏳ K8s manifests (PENDING)
│   └── terraform/            # ⏳ Infrastructure as Code (PENDING)
├── shared/                     # ✅ Cross-platform shared code (COMPLETED)
│   └── types/                # ✅ TypeScript type definitions
├── docs/                       # ✅ Documentation
│   └── Project.md            # ✅ This living document
├── .github/                    # ✅ CI/CD workflows (UPDATED)
│   └── workflows/            # ✅ GitHub Actions with monorepo support
├── .vscode/                    # ✅ VS Code workspace configuration
│   ├── settings.json          # ✅ Java, Maven, TypeScript settings
│   ├── launch.json            # ✅ Debug configurations for all services
│   └── extensions.json        # ✅ Recommended extensions
├── docker-compose.yml          # ✅ Complete development environment
├── ems.code-workspace         # ✅ Multi-folder workspace configuration
├── package.json               # ✅ Root workspace configuration
└── .gitignore                 # ✅ Comprehensive monorepo patterns
```

### 🎯 Target Structure (Full Plan)
```
ems/
├── frontend/                    # ✅ COMPLETED
├── backend/                     # ⏳ Spring Boot microservices (NEXT PHASE)
│   ├── api-gateway/            # ⏳ Spring Cloud Gateway
│   ├── auth-service/           # ⏳ JWT authentication service
│   ├── device-service/         # ⏳ Device management
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

### ✅ Phase 0: Monorepo Foundation (COMPLETED - October 2025)
1. **✅ Setup monorepo structure** - Successfully restructured project
   - Frontend moved to `/frontend` directory with full git history preservation
   - Created directory structure for backend services, infrastructure, and shared code
   - Updated CI/CD pipeline to support monorepo builds
   
2. **✅ Development Environment Setup**
   - Comprehensive `.gitignore` for all project components
   - Frontend containerization with production-ready Docker configuration
   - GitHub Actions workflow updated for monorepo structure
   
3. **✅ Frontend Verification**
   - Confirmed all existing functionality preserved in new structure
   - Build process optimized and working correctly
   - Ready for backend integration

### ✅ Phase 1: Backend Foundation (COMPLETED - October 2025)
1. **✅ Setup Spring Boot microservices structure** - Complete 5-service architecture
   - Individual Maven projects for each microservice
   - Parent POM configuration with dependency management
   - Shared library with common DTOs and utilities

2. **✅ Complete Service Infrastructure** - All services created and verified
   - **API Gateway**: Spring Cloud Gateway with routing and JWT validation
   - **Auth Service**: User authentication with PostgreSQL and Redis
   - **Device Service**: IoT device management with MQTT integration
   - **Analytics Service**: Data processing with database connectivity
   - **Notification Service**: Alert and notification management
   
3. **✅ Database and Infrastructure Setup**
   - PostgreSQL database with initialization scripts
   - Redis caching and session management
   - MQTT broker (Mosquitto) for IoT communication
   - Complete Docker Compose development environment
   
4. **✅ Development Tooling**
   - VS Code workspace configuration for monorepo
   - Debug launch configurations for all services
   - Maven build verification and service testing
   - Comprehensive IDE setup with IntelliSense

### 🎯 Phase 2: Business Logic Implementation (CURRENT - Weeks 5-8)
1. **⏳ Authentication Service APIs**
   - User registration and login endpoints
   - JWT token management and validation
   - Role-based access control implementation
   
2. **⏳ API Gateway Enhancement**
   - Complete route configuration
   - Rate limiting and security policies
   - Service discovery integration
   
3. **⏳ Device Management Implementation**
   - Device CRUD operations
   - MQTT message processing
   - Site management functionality

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

## 📊 Current Project Status (October 2025)

### ✅ Completed Milestones
- **Monorepo Foundation**: Successfully restructured from single Next.js app to organized monorepo
- **Frontend Stability**: Verified all existing functionality preserved and working
- **Complete Backend Architecture**: All 5 Spring Boot microservices created and tested
- **Database Infrastructure**: PostgreSQL, Redis, and MQTT broker configured
- **Development Environment**: VS Code workspace, Docker containers, and build system complete
- **CI/CD Integration**: GitHub Actions updated for monorepo builds with Docker multi-arch support

### 🎯 Immediate Next Steps (Priority Order)

1. **Authentication Implementation**:
   - Implement JWT authentication endpoints in auth-service
   - User registration, login, and password reset APIs
   - Integration with frontend authentication flow

2. **API Gateway Configuration**:
   - Complete service routing and load balancing
   - Rate limiting and security policy implementation
   - Request/response transformation middleware

3. **Device Management APIs**:
   - Device registration and CRUD operations
   - MQTT message processing for real-time telemetry
   - Site management and device grouping functionality

4. **Frontend-Backend Integration**:
   - Replace mock data with real API calls
   - WebSocket implementation for real-time updates
   - Error handling and loading state management

### 📈 Progress Metrics
- **Monorepo Structure**: 100% Complete ✅
- **Frontend Integration**: 100% Complete ✅
- **Backend Service Structure**: 100% Complete ✅
- **Database Infrastructure**: 100% Complete ✅
- **Development Tooling**: 100% Complete ✅
- **CI/CD Pipeline**: 100% Complete ✅
- **Business Logic APIs**: 0% Complete (Current Focus)
- **Frontend-Backend Integration**: 0% Complete (Next Phase)
- **DevOps Infrastructure**: 75% Complete (Container foundation ready)

## Next Steps

---

*This document serves as the living blueprint for the EcoGrid EMS project. It will be updated as requirements evolve and implementation progresses.*