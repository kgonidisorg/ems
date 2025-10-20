"""
Weekly Database Cleanup Utility for EMS

This script connects to the EMS Postgres databases and performs cleanup:
- Deletes all records from ems_notifications.alerts
- Deletes records from ems_devices.device_telemetry older than 7 days

Intended to be run as a cronjob every 7 days.
"""

import os
import psycopg2
from datetime import datetime, timezone, timedelta
import time
try:
    import schedule
except ImportError:
    schedule = None

def cleanup_alerts():
    conn = psycopg2.connect(
        dbname="ems_notifications",
        user=os.getenv("PGUSER", "postgres"),
        password=os.getenv("PGPASSWORD", "postgres"),
        host=os.getenv("PGHOST", "localhost"),
        port=os.getenv("PGPORT", "5432")
    )
    cutoff = datetime.now(timezone.utc) - timedelta(days=7)
    with conn:
        with conn.cursor() as cur:
            cur.execute("DELETE FROM alerts where created_at < %s;", (cutoff,))
            print("Deleted all records from ems_notifications.alerts")
    conn.close()

def cleanup_device_telemetry():
    conn = psycopg2.connect(
        dbname="ems_devices",
        user=os.getenv("PGUSER", "postgres"),
        password=os.getenv("PGPASSWORD", "postgres"),
        host=os.getenv("PGHOST", "localhost"),
        port=os.getenv("PGPORT", "5432")
    )
    cutoff = datetime.now(timezone.utc) - timedelta(days=7)
    with conn:
        with conn.cursor() as cur:
            cur.execute(
                "DELETE FROM device_telemetry WHERE created_at < %s;",
                (cutoff,)
            )
            print("Deleted old records from ems_devices.device_telemetry")
    conn.close()


def run_once():
    cleanup_alerts()
    cleanup_device_telemetry()

def run_cron():
    if schedule is None:
        raise ImportError("The 'schedule' package is required for cron mode. Install with 'pip install schedule'.")
    schedule.every(7).days.do(run_once)
    print("[DB Cleanup] Scheduled to run every 7 days. Waiting...")
    while True:
        schedule.run_pending()
        time.sleep(60)

if __name__ == "__main__":
    mode = os.getenv("CLEANUP_MODE", "once")
    if mode == "cron":
        run_cron()
    else:
        run_once()
