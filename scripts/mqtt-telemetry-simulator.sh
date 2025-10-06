#!/bin/bash

# MQTT Telemetry Simulation Script
# Sends realistic telemetry data to the first 10 EMS sites for WebSocket testing

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔋 EMS MQTT Telemetry Simulator${NC}"
echo "========================================"
echo -e "${YELLOW}MQTT Configuration:${NC}"
echo "  Using Docker container: ems-mqtt"
echo "  Sending telemetry to sites 1-10"
echo ""

# Check if Docker and MQTT container are available
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed or not running${NC}"
    exit 1
fi

if ! docker ps | grep -q "ems-mqtt"; then
    echo -e "${RED}❌ EMS MQTT container (ems-mqtt) is not running${NC}"
    echo "Please start the EMS services: docker compose up -d"
    exit 1
fi

# Function to generate realistic BMS telemetry
generate_bms_telemetry() {
    local site_id=$1
    local device_num=$2
    local base_soc=$3
    
    # Add some realistic variation
    local soc=$(echo "scale=1; $base_soc + (($RANDOM % 10) - 5) * 0.1" | bc)
    local voltage=$(echo "scale=1; 750 + ($RANDOM % 100)" | bc)
    local current=$(echo "scale=1; (($RANDOM % 200) - 100) * 0.1" | bc)
    local temperature=$(echo "scale=1; 25 + ($RANDOM % 20)" | bc)
    local power=$(echo "scale=1; $current * $voltage / 1000" | bc)
    local health=$(echo "scale=1; 90 + ($RANDOM % 10)" | bc)
    
    cat << EOF
{
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)",
  "device_id": "BMS-SITE${site_id}-$(printf "%03d" $device_num)",
  "soc": $soc,
  "voltage": $voltage,
  "current": $current,
  "temperature": $temperature,
  "power": $power,
  "energy_today": $(echo "scale=1; 50 + ($RANDOM % 100)" | bc),
  "cycle_count": $((1000 + RANDOM % 500)),
  "health": $health,
  "status": "ONLINE"
}
EOF
}

# Function to generate realistic Solar telemetry
generate_solar_telemetry() {
    local site_id=$1
    local hour=$(date +%H)
    
    # Solar power varies by time of day
    local base_power=0
    if [ $hour -ge 6 ] && [ $hour -le 18 ]; then
        # Daylight hours - simulate solar curve
        local time_factor=$(echo "scale=2; s(($hour - 12) * 3.14159 / 12)" | bc -l)
        base_power=$(echo "scale=1; 800 * (1 + $time_factor)" | bc)
        if (( $(echo "$base_power < 0" | bc -l) )); then
            base_power=0
        fi
    fi
    
    local power=$(echo "scale=1; $base_power + ($RANDOM % 100)" | bc)
    local voltage=$(echo "scale=1; 600 + ($RANDOM % 100)" | bc)
    local current=$(echo "scale=1; $power / $voltage * 1000" | bc)
    local temperature=$(echo "scale=1; 30 + ($RANDOM % 25)" | bc)
    local irradiance=$(echo "scale=0; $power * 1.2" | bc)
    local efficiency=$(echo "scale=1; 18 + ($RANDOM % 4)" | bc)
    
    cat << EOF
{
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)",
  "device_id": "SOLAR-SITE${site_id}-001",
  "power": $power,
  "voltage": $voltage,
  "current": $current,
  "temperature": $temperature,
  "irradiance": $irradiance,
  "energy_today": $(echo "scale=1; 200 + ($RANDOM % 300)" | bc),
  "efficiency": $efficiency,
  "status": "ONLINE"
}
EOF
}

# Function to generate realistic EV Charger telemetry
generate_ev_telemetry() {
    local site_id=$1
    local hour=$(date +%H)
    
    # EV charging is higher during business hours and evening
    local active_sessions=0
    if [ $hour -ge 8 ] && [ $hour -le 10 ] || [ $hour -ge 17 ] && [ $hour -le 20 ]; then
        active_sessions=$((1 + RANDOM % 4))
    elif [ $hour -ge 11 ] && [ $hour -le 16 ]; then
        active_sessions=$((RANDOM % 3))
    fi
    
    local power_per_session=$(echo "scale=1; 50 + ($RANDOM % 100)" | bc)
    local total_power=$(echo "scale=1; $active_sessions * $power_per_session" | bc)
    local voltage=$(echo "scale=1; 400 + ($RANDOM % 200)" | bc)
    local current=$(echo "scale=1; $total_power / $voltage * 1000" | bc)
    local temperature=$(echo "scale=1; 20 + ($RANDOM % 15)" | bc)
    
    local status="AVAILABLE"
    if [ $active_sessions -gt 0 ]; then
        status="CHARGING"
    fi
    
    cat << EOF
{
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)",
  "device_id": "EV-SITE${site_id}-001",
  "power": $total_power,
  "voltage": $voltage,
  "current": $current,
  "energy_delivered": $(echo "scale=1; 100 + ($RANDOM % 200)" | bc),
  "active_sessions": $active_sessions,
  "temperature": $temperature,
  "status": "$status"
}
EOF
}

# Function to publish telemetry using Docker container
publish_telemetry() {
    local topic=$1
    local payload=$2
    
    # Use docker exec to run mosquitto_pub inside the MQTT container
    echo "$payload" | docker exec -i ems-mqtt mosquitto_pub -h localhost -t "$topic" -s
}

# Main simulation loop
echo -e "${BLUE}🚀 Starting telemetry simulation...${NC}"
echo "Press Ctrl+C to stop"
echo ""

# Initialize SOC values for BMS devices (will vary over time)
declare -a bms_soc
for i in {1..20}; do
    bms_soc[$i]=$(echo "scale=1; 80 + ($RANDOM % 15)" | bc)
done

iteration=0
while true; do
    iteration=$((iteration + 1))
    echo -e "${YELLOW}📡 Sending telemetry batch #$iteration at $(date)${NC}"
    
    # Send telemetry for sites 1-10
    for site_id in {1..10}; do
        echo -n "  Site $site_id: "
        
        # BMS Device 1
        bms1_idx=$((site_id * 2 - 1))
        bms1_payload=$(generate_bms_telemetry $site_id 1 ${bms_soc[$bms1_idx]})
        publish_telemetry "ecogrid/site${site_id}/bms/001" "$bms1_payload"
        echo -n "BMS1 "
        
        # BMS Device 2
        bms2_idx=$((site_id * 2))
        bms2_payload=$(generate_bms_telemetry $site_id 2 ${bms_soc[$bms2_idx]})
        publish_telemetry "ecogrid/site${site_id}/bms/002" "$bms2_payload"
        echo -n "BMS2 "
        
        # Solar Array
        solar_payload=$(generate_solar_telemetry $site_id)
        publish_telemetry "ecogrid/site${site_id}/solar/001" "$solar_payload"
        echo -n "SOLAR "
        
        # EV Charger
        ev_payload=$(generate_ev_telemetry $site_id)
        publish_telemetry "ecogrid/site${site_id}/ev/001" "$ev_payload"
        echo -n "EV "
        
        echo -e "${GREEN}✓${NC}"
        
        # Small delay between sites
        sleep 0.1
    done
    
    # Slowly vary SOC values for next iteration
    for i in {1..20}; do
        # Simulate slow charging/discharging
        variation=$(echo "scale=1; (($RANDOM % 3) - 1) * 0.5" | bc)
        new_soc=$(echo "scale=1; ${bms_soc[$i]} + $variation" | bc)
        
        # Keep SOC within realistic bounds
        if (( $(echo "$new_soc > 95" | bc -l) )); then
            new_soc=95
        elif (( $(echo "$new_soc < 20" | bc -l) )); then
            new_soc=20
        fi
        
        bms_soc[$i]=$new_soc
    done
    
    echo -e "${GREEN}📊 Batch $iteration completed - sent data for 40 devices${NC}"
    echo ""
    
    # Wait 10 seconds before next batch
    sleep 10
done