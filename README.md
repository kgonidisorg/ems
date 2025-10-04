# EcoGrid Energy Management System - Monorepo

This monorepo contains the complete EcoGrid Energy Management System, including frontend, backend services, and infrastructure code.

## Table of Contents
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Available Scripts](#available-scripts)
- [Current Status](#current-status)
- [Documentation](#documentation)
- [Technology Stack](#technology-stack)

## Project Structure

```
ems/
├── frontend/                    # Next.js 15 application
├── backend/                     # Spring Boot microservices (coming soon)
├── infrastructure/              # DevOps and Infrastructure
│   ├── docker/                 # Dockerfiles
│   ├── kubernetes/             # K8s manifests and Helm charts
│   └── terraform/              # Infrastructure as Code
├── shared/                      # Cross-platform shared code
├── docs/                        # Documentation
├── .github/                     # CI/CD workflows
└── README.md                    # This file
```

## Quick Start

### Prerequisites
- Node.js 18+ and npm 9+
- Docker and Docker Compose (for full stack development)

### Development

1. **Install dependencies for all workspaces:**
   ```bash
   npm run install:all
   ```

2. **Start the frontend development server:**
   ```bash
   npm run dev
   # or specifically:
   npm run frontend:dev
   ```

3. **Build the frontend:**
   ```bash
   npm run build
   # or specifically:
   npm run frontend:build
   ```

## Available Scripts

- `npm run dev` - Start frontend development server
- `npm run build` - Build frontend for production
- `npm run start` - Start frontend production server
- `npm run lint` - Run linting on frontend
- `npm run clean` - Clean all node_modules and build artifacts
- `npm run docker:dev` - Start development environment with Docker
- `npm run docker:build` - Build Docker images
- `npm run docker:down` - Stop Docker containers

### Docker Development

To run the full development environment:

```bash
npm run docker:dev
```

This will start:
- Frontend development server at http://localhost:3000
- Backend services (when implemented)
- Databases and message queues

## Current Status

### ✅ Completed
- **Frontend**: Next.js 15 application with Tailwind CSS, TypeScript, and Recharts
- **Dashboard**: Real-time KPIs, interactive maps, analytics pages
- **Monorepo Structure**: Organized workspace with npm workspaces
- **Docker Setup**: Development and production containers

### 🔄 In Progress
- **Backend Services**: Spring Boot microservices architecture
- **Real-time Data**: MQTT + WebSocket integration
- **Authentication**: JWT-based user management

### 📋 Planned
- **DevOps**: CI/CD pipelines, Kubernetes deployment
- **Infrastructure**: Terraform for cloud resources
- **Advanced Features**: Device control, predictive analytics

## Documentation

See the [docs/](./docs/) directory for detailed documentation:

- [Project.md](./docs/Project.md) - Complete project plan and architecture
- Backend API documentation (coming soon)
- Deployment guides (coming soon)

## Contributing

1. Follow the existing code structure and naming conventions
2. Add tests for new features
3. Update documentation as needed
4. Use the provided linting and formatting tools
- Use the provided linting and formatting tools

## Technology Stack

### Frontend
- **Framework**: Next.js 15 with TypeScript
- **Styling**: Tailwind CSS
- **Charts**: Recharts
- **Maps**: Mapbox GL JS
- **Icons**: React Icons

### Backend (Planned)
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL, Redis
- **Message Queue**: Apache Kafka
- **IoT Protocol**: MQTT
- **API**: REST + GraphQL

### DevOps
- **Containers**: Docker & Docker Compose
- **Orchestration**: Kubernetes
- **CI/CD**: GitHub Actions
- **Infrastructure**: Terraform
- **Monitoring**: Prometheus, Grafana

## License

MIT License - see LICENSE file for details.
```

## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/kgonidisorg/ems.git
   ```
2. Navigate to the project directory:
   ```bash
   cd ems
   ```
3. Install dependencies:
   ```bash
   npm install
   ```

## Development
To start the development server:
```bash
npm run dev
```

## Docker Setup
Build and run the application using Docker:
```bash
docker-compose up --build
```

## GitHub Actions
This repository includes a GitHub Actions workflow for building and pushing Docker images to GitHub Container Registry (GHCR). Ensure repository variables are correctly configured for dynamic Docker image tagging.

## Contributing
1. Fork the repository.
2. Create a new branch:
   ```bash
   git checkout -b feature-name
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add feature-name"
   ```
4. Push to your branch:
   ```bash
   git push origin feature-name
   ```
5. Create a pull request.

## License
This project is licensed under the MIT License. See the LICENSE file for details.

## Contact
For questions or feedback, please contact [kiron.gonidis@gmail.com].