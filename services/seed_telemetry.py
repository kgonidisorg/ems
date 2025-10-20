"""
EMS MQTT Telemetry Simulator - Payload Classes

This module defines Python classes for generating realistic telemetry payloads for:
- Battery Management System (BMS)
- Solar Array
- EV Charger

Payloads are based on the Java DTOs provided in the EMS backend.
Includes caching and value update utilities for realistic simulation.
"""

import datetime
import math
import random
import time
import json
import os
import paho.mqtt.client as mqtt
from typing import Any, Dict, List, Optional, Tuple


MQTT_HOST = os.getenv("MQTT_HOST", "localhost")
MQTT_PORT = int(os.getenv("MQTT_PORT", "1883"))
MQTT_USERNAME = os.getenv("MQTT_USERNAME", None)
MQTT_PASSWORD = os.getenv("MQTT_PASSWORD", None)


# Utility for updating cached values
def update_value(current, min_value, max_value, max_delta=1.0, decimals=1):
    """
    Update a numeric value within [min_value, max_value],
    changing by at most max_delta from current, rounded to decimals.
    """
    delta = random.uniform(-max_delta, max_delta)
    new_value = current + delta
    new_value = max(min_value, min(max_value, new_value))
    return round(new_value, decimals)


def update_status(current, allowed, change_prob=0.05):
    """
    Update a status string, changing only with probability change_prob.
    """
    if random.random() < change_prob:
        choices = [s for s in allowed if s != current]
        return random.choice(choices) if choices else current
    return current


# Caching for (siteId, deviceId)
class TelemetryCache:
    def __init__(self):
        self.cache: Dict[Tuple[int, str], Dict[str, Any]] = {}

    def get(self, site_id: int, device_type: str):
        return self.cache.get((site_id, device_type))

    def set(self, site_id: int, device_type: str, values: Dict[str, Any]):
        self.cache[(site_id, device_type)] = values


telemetry_cache = TelemetryCache()


class BMSTelemetry:
    def __init__(self, site_id: int, device_num: int):
        device_id = (site_id - 1) * 4 + device_num
        now = datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0).isoformat().replace('+00:00', '')
        cache = telemetry_cache.get(site_id, f"bms{device_num}")
        if cache:
            self.deviceId = device_id
            self.timestamp = now
            self.soc = update_value(cache["soc"], 20, 95, max_delta=0.5)
            self.remainingCapacity = update_value(
                cache["remainingCapacity"], 40, 60, max_delta=1.0
            )
            self.nominalCapacity = update_value(
                cache["nominalCapacity"], 60, 80, max_delta=1.0
            )
            self.chargeRate = update_value(cache["chargeRate"], -10, 10, max_delta=1.0)
            self.voltage = update_value(cache["voltage"], 700, 850, max_delta=5.0)
            self.current = update_value(cache["current"], -100, 100, max_delta=5.0)
            self.temperature = update_value(cache["temperature"], 25, 45, max_delta=1.0)
            self.moduleTemperatures = [
                update_value(mt, 24, 30, max_delta=0.5)
                for mt in cache["moduleTemperatures"]
            ]
            self.healthStatus = update_status(
                cache["healthStatus"], ["EXCELLENT", "GOOD", "FAIR", "POOR"]
            )
            self.efficiency = update_value(cache["efficiency"], 90, 99, max_delta=0.5)
            self.cycleCount = cache["cycleCount"] + random.randint(0, 2)
            self.alarms = cache["alarms"]
            self.warnings = cache["warnings"]
            self.lastMaintenance = cache["lastMaintenance"]
        else:
            self.deviceId = device_id
            self.timestamp = now
            self.soc = round(random.uniform(20, 95), 1)
            self.remainingCapacity = round(random.uniform(40, 60), 1)
            self.nominalCapacity = round(random.uniform(60, 80), 1)
            self.chargeRate = round(random.uniform(-10, 10), 1)
            self.voltage = round(random.uniform(700, 850), 1)
            self.current = round(random.uniform(-100, 100), 1)
            self.temperature = round(random.uniform(25, 45), 1)
            self.moduleTemperatures = [
                round(random.uniform(24, 30), 1) for _ in range(3)
            ]
            self.healthStatus = random.choice(["EXCELLENT", "GOOD", "FAIR", "POOR"])
            self.efficiency = round(random.uniform(90, 99), 1)
            self.cycleCount = random.randint(1000, 1500)
            self.alarms = []
            self.warnings = []
            self.lastMaintenance = now
        telemetry_cache.set(site_id, f"bms{device_num}", self.to_dict())

    def to_dict(self):
        return {
            "deviceId": self.deviceId,
            "timestamp": self.timestamp,
            "soc": self.soc,
            "remainingCapacity": self.remainingCapacity,
            "nominalCapacity": self.nominalCapacity,
            "chargeRate": self.chargeRate,
            "voltage": self.voltage,
            "current": self.current,
            "temperature": self.temperature,
            "moduleTemperatures": self.moduleTemperatures,
            "healthStatus": self.healthStatus,
            "efficiency": self.efficiency,
            "cycleCount": self.cycleCount,
            "alarms": self.alarms,
            "warnings": self.warnings,
            "lastMaintenance": self.lastMaintenance,
        }


class SolarArrayTelemetry:
    class StringData:
        def __init__(self, cache=None, string_id: str = "STR1"):
            if cache:
                self.stringId = string_id
                self.voltage = update_value(
                    cache["voltage"], 600, 650, max_delta=2, decimals=2
                )
                self.current = update_value(
                    cache["current"], 10, 20, max_delta=1, decimals=2
                )
                self.power = round(self.voltage * self.current / 1000, 2)
                self.temperature = update_value(
                    cache["temperature"], 30, 40, max_delta=1, decimals=2
                )
            else:
                self.stringId = string_id
                self.voltage = round(random.uniform(600, 650), 2)
                self.current = round(random.uniform(10, 20), 2)
                self.power = round(self.voltage * self.current / 1000, 2)
                self.temperature = round(random.uniform(30, 40), 2)

        def to_dict(self):
            return {
                "stringId": self.stringId,
                "voltage": self.voltage,
                "current": self.current,
                "power": self.power,
                "temperature": self.temperature,
            }

    def __init__(self, site_id: int):
        device_id = (site_id - 1) * 4 + 3
        now = datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0).isoformat().replace('+00:00', '')
        hour = datetime.datetime.now(datetime.timezone.utc).hour
        cache = telemetry_cache.get(site_id, "solar")
        # Simulate solar output curve
        base_power = 0
        if 6 <= hour <= 18:
            time_factor = math.sin((hour - 12) * 3.14159 / 12)
            base_power = max(0, 800 * (1 + time_factor))
        if cache:
            self.deviceId = device_id
            self.timestamp = now
            self.currentOutput = update_value(
                cache["currentOutput"], 0, 900, max_delta=10
            )
            self.energyYield = update_value(
                cache["energyYield"], 200, 500, max_delta=10
            )
            self.energyYieldTotal = update_value(
                cache["energyYieldTotal"], 10000, 15000, max_delta=50
            )
            self.panelTemperature = update_value(
                cache["panelTemperature"], 30, 55, max_delta=2
            )
            self.irradiance = round(self.currentOutput * 1.2, 0)
            self.ambientTemperature = update_value(
                cache["ambientTemperature"], 20, 35, max_delta=1
            )
            self.windSpeed = update_value(
                cache["windSpeed"], 0, 1, max_delta=0.1, decimals=2
            )
            self.inverterEfficiency = update_value(
                cache["inverterEfficiency"], 95, 99, max_delta=0.2
            )
            self.systemEfficiency = update_value(
                cache["systemEfficiency"], 90, 99, max_delta=0.2
            )
            self.performanceRatio = update_value(
                cache["performanceRatio"], 80, 99, max_delta=0.5
            )
            self.stringData = [
                self.StringData(cache_sd, f"STR{i+1}").to_dict()
                for i, cache_sd in enumerate(cache["stringData"])
            ]
            self.inverterStatus = update_status(
                cache["inverterStatus"], ["ONLINE", "OFFLINE", "FAULT"]
            )
            self.alarms = cache["alarms"]
            self.lastCleaning = cache["lastCleaning"]
        else:
            self.deviceId = device_id
            self.timestamp = now
            self.currentOutput = round(base_power + random.uniform(0, 100), 1)
            self.energyYield = round(random.uniform(200, 500), 1)
            self.energyYieldTotal = round(random.uniform(10000, 15000), 1)
            self.panelTemperature = round(random.uniform(30, 55), 1)
            self.irradiance = round(self.currentOutput * 1.2, 0)
            self.ambientTemperature = round(random.uniform(20, 35), 1)
            self.windSpeed = round(random.uniform(0, 1), 2)
            self.inverterEfficiency = round(random.uniform(95, 99), 1)
            self.systemEfficiency = round(random.uniform(90, 99), 1)
            self.performanceRatio = round(random.uniform(80, 99), 1)
            self.stringData = [
                self.StringData(None, f"STR{i+1}").to_dict()
                for i in range(random.randint(1, 3))
            ]
            self.inverterStatus = random.choice(["ONLINE", "OFFLINE", "FAULT"])
            self.alarms = ["OVERVOLTAGE", "UNDERVOLTAGE"]
            self.lastCleaning = now
        telemetry_cache.set(site_id, "solar", self.to_dict())

    def to_dict(self):
        return {
            "deviceId": self.deviceId,
            "timestamp": self.timestamp,
            "currentOutput": self.currentOutput,
            "energyYield": self.energyYield,
            "energyYieldTotal": self.energyYieldTotal,
            "panelTemperature": self.panelTemperature,
            "irradiance": self.irradiance,
            "ambientTemperature": self.ambientTemperature,
            "windSpeed": self.windSpeed,
            "inverterEfficiency": self.inverterEfficiency,
            "systemEfficiency": self.systemEfficiency,
            "performanceRatio": self.performanceRatio,
            "stringData": self.stringData,
            "inverterStatus": self.inverterStatus,
            "alarms": self.alarms,
            "lastCleaning": self.lastCleaning,
        }


class EVChargerTelemetry:
    class ChargerData:
        def __init__(self, cache=None, charger_id: str = "CHG1"):
            if cache:
                self.chargerId = charger_id
                self.status = update_status(
                    cache["status"],
                    ["AVAILABLE", "OCCUPIED", "CHARGING", "FAULT"],
                    change_prob=0.02,
                )
                self.sessionId = cache["sessionId"]
                self.powerOutput = update_value(
                    cache["powerOutput"], 20, 50, max_delta=2
                )
                self.sessionDuration = update_value(
                    cache["sessionDuration"], 10, 40, max_delta=2
                )
                self.energyDelivered = update_value(
                    cache["energyDelivered"], 5, 25, max_delta=1
                )
                self.connectorType = cache["connectorType"]
            else:
                self.chargerId = charger_id
                self.status = "CHARGING"
                self.sessionId = f"SID{random.randint(1000,9999)}"
                self.powerOutput = round(random.uniform(20, 50), 1)
                self.sessionDuration = round(random.uniform(10, 40), 1)
                self.energyDelivered = round(random.uniform(5, 25), 1)
                self.connectorType = "CCS"

        def to_dict(self):
            return {
                "chargerId": self.chargerId,
                "status": self.status,
                "sessionId": self.sessionId,
                "powerOutput": self.powerOutput,
                "sessionDuration": self.sessionDuration,
                "energyDelivered": self.energyDelivered,
                "connectorType": self.connectorType,
            }

    def __init__(self, site_id: int):
        device_id = (site_id - 1) * 4 + 4
        now = datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0).isoformat().replace('+00:00', '')
        cache = telemetry_cache.get(site_id, "ev")
        if cache:
            active_sessions = update_value(
                cache["activeSessions"], 0, 3, max_delta=1, decimals=0
            )
            self.deviceId = device_id
            self.timestamp = now
            self.activeSessions = int(active_sessions)
            self.totalSessions = int(active_sessions) + random.randint(0, 2)
            self.powerDelivered = update_value(
                cache["powerDelivered"], 50, 150, max_delta=5
            )
            self.energyDelivered = update_value(
                cache["energyDelivered"], 100, 300, max_delta=10
            )
            self.chargerData = [
                self.ChargerData(cache_cd, f"CHG{i+1}").to_dict()
                for i, cache_cd in enumerate(cache["chargerData"])
            ]
            self.revenue = update_value(
                cache["revenue"], 50, 100, max_delta=2, decimals=2
            )
            self.avgSessionDuration = update_value(
                cache["avgSessionDuration"], 20, 60, max_delta=2
            )
            self.utilizationRate = update_value(
                cache["utilizationRate"], 0, 100, max_delta=5
            )
            self.networkConnectivity = cache["networkConnectivity"]
            self.paymentSystemStatus = update_status(
                cache["paymentSystemStatus"], ["ONLINE", "OFFLINE"], change_prob=0.01
            )
            self.faults = update_value(cache["faults"], 0, 2, max_delta=1, decimals=0)
            self.uptime = update_value(cache["uptime"], 95, 99, max_delta=0.2)
        else:
            active_sessions = random.randint(0, 3)
            self.deviceId = device_id
            self.timestamp = now
            self.activeSessions = active_sessions
            self.totalSessions = active_sessions + random.randint(0, 2)
            self.powerDelivered = round(random.uniform(50, 150), 1)
            self.energyDelivered = round(random.uniform(100, 300), 1)
            self.chargerData = [
                self.ChargerData(None, f"CHG{i+1}").to_dict()
                for i in range(random.randint(1, 3))
            ]
            self.revenue = round(random.uniform(50, 100), 2)
            self.avgSessionDuration = round(random.uniform(20, 60), 1)
            self.utilizationRate = round((active_sessions / 10) * 100, 1)
            self.networkConnectivity = True
            self.paymentSystemStatus = random.choice(["ONLINE", "OFFLINE"])
            self.faults = random.randint(0, 2)
            self.uptime = round(random.uniform(95, 99), 1)
        telemetry_cache.set(site_id, "ev", self.to_dict())

    def to_dict(self):
        return {
            "deviceId": self.deviceId,
            "timestamp": self.timestamp,
            "activeSessions": self.activeSessions,
            "totalSessions": self.totalSessions,
            "powerDelivered": self.powerDelivered,
            "energyDelivered": self.energyDelivered,
            "chargerData": self.chargerData,
            "revenue": self.revenue,
            "avgSessionDuration": self.avgSessionDuration,
            "utilizationRate": self.utilizationRate,
            "networkConnectivity": self.networkConnectivity,
            "paymentSystemStatus": self.paymentSystemStatus,
            "faults": self.faults,
            "uptime": self.uptime,
        }


def get_mqtt_client():
    if mqtt is None:
        raise ImportError(
            "paho-mqtt is required. Install with 'pip install paho-mqtt'."
        )
    client = mqtt.Client()
    if MQTT_USERNAME and MQTT_PASSWORD:
        client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
    client.connect(MQTT_HOST, MQTT_PORT)
    return client


def publish_telemetry(client, topic, payload):
    client.publish(topic, json.dumps(payload), qos=0, retain=False)


def main():
    print(
        f"Starting EMS MQTT Telemetry Simulator. Publishing to {MQTT_HOST}:{MQTT_PORT}"
    )
    client = get_mqtt_client()
    count = 0
    sleep_time = 10
    print_frequency = 30
    while True:
        batch_time = datetime.datetime.now(datetime.timezone.utc).isoformat()
        if count % print_frequency == 0:
            print(f"Publishing {print_frequency} telemetry batches at {batch_time} over {print_frequency * sleep_time} seconds")
        count += 1
        for site_id in range(1, 11):
            # BMS Device 1
            bms1 = BMSTelemetry(site_id, 1)
            publish_telemetry(client, f"ecogrid/site{site_id}/bms/001", bms1.to_dict())
            # BMS Device 2
            bms2 = BMSTelemetry(site_id, 2)
            publish_telemetry(client, f"ecogrid/site{site_id}/bms/002", bms2.to_dict())
            # Solar Array
            solar = SolarArrayTelemetry(site_id)
            publish_telemetry(client, f"ecogrid/site{site_id}/solar/001", solar.to_dict())
            # EV Charger
            ev = EVChargerTelemetry(site_id)
            publish_telemetry(client, f"ecogrid/site{site_id}/ev/001", ev.to_dict())
            time.sleep(0.1)
        time.sleep(sleep_time)


if __name__ == "__main__":
    main()
