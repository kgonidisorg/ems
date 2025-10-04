# Message Queue Integration

This document describes the event-driven architecture and message queue integration in the EcoGrid EMS system.

## Architecture Overview

The EMS system uses a hybrid messaging approach:

1. **Internal Events**: Spring Application Events for synchronous processing within services
2. **Inter-Service Communication**: Apache Kafka for asynchronous event streaming
3. **IoT Communication**: MQTT for device telemetry and commands

## Kafka Topics

### Device Events

#### `device-telemetry`
- **Purpose**: Real-time device telemetry data
- **Producer**: Device Service
- **Consumers**: Analytics Service, Notification Service
- **Partition Key**: Device Serial Number
- **Retention**: 7 days

**Event Schema**:
```json
{
  "deviceId": 123,
  "serialNumber": "INV-001",
  "siteId": 1,
  "deviceType": "SOLAR_INVERTER",
  "telemetryData": {
    "powerKw": 45.2,
    "voltage": 480.5,
    "current": 94.3,
    "temperature": 35.2,
    "efficiency": 98.5
  },
  "timestamp": "2025-10-04T10:15:00Z",
  "eventType": "DEVICE_TELEMETRY"
}
```

#### `device-status`
- **Purpose**: Device status changes (online/offline/maintenance/error)
- **Producer**: Device Service
- **Consumers**: Notification Service, Analytics Service
- **Partition Key**: Device Serial Number
- **Retention**: 30 days

**Event Schema**:
```json
{
  "deviceId": 123,
  "serialNumber": "INV-001",
  "siteId": 1,
  "previousStatus": "OFFLINE",
  "newStatus": "ONLINE",
  "timestamp": "2025-10-04T10:15:00Z",
  "eventType": "DEVICE_STATUS_CHANGE",
  "reason": "Device communication restored"
}
```

## MQTT Topics

### Device Communication

#### Telemetry Data
- **Topic Pattern**: `sites/{siteId}/devices/{serialNumber}/telemetry`
- **Direction**: Device → EMS
- **QoS**: 1 (At least once delivery)
- **Payload**: JSON telemetry data

#### Device Commands
- **Topic Pattern**: `sites/{siteId}/devices/{serialNumber}/commands`
- **Direction**: EMS → Device
- **QoS**: 2 (Exactly once delivery)
- **Payload**: JSON command structure

#### Status Updates
- **Topic Pattern**: `sites/{siteId}/devices/{serialNumber}/status`
- **Direction**: Device → EMS
- **QoS**: 1 (At least once delivery)
- **Payload**: JSON status information

## Event Flow

### Device Telemetry Flow
```
IoT Device → MQTT → Device Service → Kafka → Analytics Service
                                  ↓
                              Notification Service
```

### Device Status Change Flow
```
Device Service → Kafka → Notification Service → WebSocket → Frontend
                     ↓
               Analytics Service → Database
```

## Configuration

### Kafka Configuration
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
    consumer:
      group-id: device-service
      auto-offset-reset: earliest
```

### MQTT Configuration
```yaml
mqtt:
  broker-url: tcp://localhost:1883
  client-id: device-service
  username: ems_mqtt
  password: ems_mqtt_password
```

## Development Setup

### Using Docker Compose
```bash
# Start infrastructure services (Kafka, MQTT, Databases)
docker-compose up -d postgres redis mosquitto zookeeper kafka

# Start backend services
docker-compose --profile backend up -d
```

### Manual Setup
1. Start Zookeeper: `bin/zookeeper-server-start.sh config/zookeeper.properties`
2. Start Kafka: `bin/kafka-server-start.sh config/server.properties`
3. Start MQTT Broker: `mosquitto -c mosquitto.conf`

## Monitoring

### Kafka Topics
```bash
# List topics
kafka-topics --bootstrap-server localhost:9092 --list

# Describe topic
kafka-topics --bootstrap-server localhost:9092 --describe --topic device-telemetry

# Monitor messages
kafka-console-consumer --bootstrap-server localhost:9092 --topic device-telemetry --from-beginning
```

### MQTT Messages
```bash
# Subscribe to telemetry
mosquitto_sub -h localhost -t "sites/+/devices/+/telemetry"

# Publish test message
mosquitto_pub -h localhost -t "sites/1/devices/INV-001/telemetry" -m '{"powerKw": 45.2}'
```

## Error Handling

- **Dead Letter Topics**: Failed messages are routed to `{topic-name}-dlt`
- **Retry Logic**: 3 retries with exponential backoff
- **Circuit Breaker**: Kafka producer failures trigger circuit breaker
- **Monitoring**: Health checks monitor Kafka connectivity

## Security

- **Kafka**: SASL_SSL authentication (production)
- **MQTT**: TLS encryption with client certificates
- **Network**: Internal service communication only
- **Authorization**: Topic-level ACLs for service access

## Performance Considerations

- **Partitioning**: Device serial number as partition key for load distribution
- **Batch Processing**: Consumer batching for high-throughput scenarios
- **Compression**: Gzip compression for large telemetry payloads
- **Retention**: Automatic cleanup based on retention policies