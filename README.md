# EMS Dashboard and API Documentation

## Table of Contents
- [EMS Dashboard and API Documentation](#ems-dashboard-and-api-documentation)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
  - [Features](#features)
    - [EMS Dashboard](#ems-dashboard)
    - [API Documentation](#api-documentation)
  - [Project Structure](#project-structure)
  - [Installation](#installation)
  - [Development](#development)
  - [Docker Setup](#docker-setup)
  - [GitHub Actions](#github-actions)
  - [Contributing](#contributing)
  - [License](#license)
  - [Contact](#contact)

## Overview
This repository contains the codebase for the Energy Management System (EMS) dashboard and API documentation. The EMS dashboard provides tools for monitoring and controlling energy systems, while the API documentation offers an interactive interface for developers to explore and test API endpoints.

## Features
### EMS Dashboard
- **One-Click Controls**: Simplified controls for managing energy systems.
- **Threshold Alarms**: Alerts for exceeding predefined energy thresholds.
- **Responsive Design**: Optimized for various screen sizes.

### API Documentation
- **Interactive API Tester**: Test API endpoints directly from the documentation.
- **Responsive Layout**: Accessible on both desktop and mobile devices.

## Project Structure
```
├── description
├── docker-compose.yml
├── Dockerfile
├── eslint.config.mjs
├── next-env.d.ts
├── next.config.ts
├── package.json
├── postcss.config.mjs
├── README.md
├── tsconfig.json
├── public/
│   ├── banner.jpg
│   ├── bms_cabinet.obj
│   ├── bms.webp
│   ├── facility.webp
│   ├── file.svg
│   ├── flow.jpg
│   ├── flow.webp
│   ├── globe.svg
│   ├── green-energy-icon.svg
│   ├── next.svg
│   ├── solar.webp
│   ├── system.jpg
│   ├── vercel.svg
│   ├── Whisk_storyboard9b8c32f94ee84df9b74c2476.jpg
│   ├── window.svg
├── src/
│   ├── app/
│   │   ├── favicon.ico
│   │   ├── globals.css
│   │   ├── layout.tsx
│   │   ├── page.tsx
│   │   ├── analytics/
│   │   │   ├── page.tsx
│   │   ├── apidocs/
│   │   │   ├── page.tsx
│   │   ├── ems/
│   │   │   ├── page.tsx
│   │   ├── network/
│   │   │   ├── page.tsx
│   ├── components/
│   │   ├── ClusterMapView.tsx
│   │   ├── CO2Chart.tsx
│   │   ├── EnergyFlowDiagram.tsx
│   │   ├── Filters.tsx
│   │   ├── FinancialDashboard.tsx
│   │   ├── KPIStrip.tsx
│   │   ├── MapView.tsx
│   │   ├── ReportScheduler.tsx
│   │   ├── Sidebar.tsx
│   │   ├── TimeSeriesGraph.tsx
│   │   ├── Topbar.tsx
│   │   ├── ui.tsx
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