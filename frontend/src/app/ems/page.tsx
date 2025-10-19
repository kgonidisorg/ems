"use client";
import React, { useState, useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import EnergyFlowDiagram from "@/components/EnergyFlowDiagram";
import Topbar from "@/components/Topbar";
import { SiteSelector } from "@/components/SiteSelector";
import { useSiteOptions } from "@/hooks/useSiteOptions";
import { useSiteOverview } from "@/hooks/useSiteOverview";
import {
    FaBatteryFull,
    FaBolt,
    FaThermometerHalf,
    FaTag,
    FaHeartbeat,
    FaSyncAlt,
    FaSun,
    FaChartLine,
    FaCloudSun,
    FaTools,
    FaPlug,
    FaClock,
    FaDollarSign,
    FaExclamationTriangle,
    FaMapMarkerAlt,
    FaPhone,
    FaGlobe,
} from "react-icons/fa";

const EMSPageContent: React.FC = () => {
    const router = useRouter();
    const searchParams = useSearchParams();
    const [selectedSiteId, setSelectedSiteId] = useState<number | null>(null);

    // Fetch available sites for dropdown
    const { sites } = useSiteOptions();

    // Initialize selectedSiteId from URL parameter or auto-select first site
    useEffect(() => {
        const urlSiteId = searchParams.get("siteId");

        if (urlSiteId) {
            // Use site ID from URL if valid
            const siteIdNumber = parseInt(urlSiteId, 10);
            if (!isNaN(siteIdNumber)) {
                setSelectedSiteId(siteIdNumber);
                return;
            }
        }

        // Auto-select the first site when sites are loaded and no site is selected
        if (sites.length > 0 && selectedSiteId === null) {
            const firstSiteId = sites[0].id;
            setSelectedSiteId(firstSiteId);
            // Update URL to reflect the auto-selected site
            const newSearchParams = new URLSearchParams(
                searchParams.toString()
            );
            newSearchParams.set("siteId", firstSiteId.toString());
            router.replace(`/ems?${newSearchParams.toString()}`);
        }
    }, [sites, selectedSiteId, searchParams, router]);

    // Handle site selection change and update URL
    const handleSiteChange = (siteId: number | null) => {
        setSelectedSiteId(siteId);

        if (siteId !== null) {
            const newSearchParams = new URLSearchParams(
                searchParams.toString()
            );
            newSearchParams.set("siteId", siteId.toString());
            router.replace(`/ems?${newSearchParams.toString()}`);
        } else {
            // Remove siteId parameter if null
            const newSearchParams = new URLSearchParams(
                searchParams.toString()
            );
            newSearchParams.delete("siteId");
            const queryString = newSearchParams.toString();
            router.replace(`/ems${queryString ? `?${queryString}` : ""}`);
        }
    };

    // Fetch site overview data based on selected site
    const { data: siteOverview } = useSiteOverview({ siteId: selectedSiteId });

    // Extract battery system data from site overview
    const batteryDevices =
        siteOverview?.devices.filter((d) => d.deviceType === "BMS") || [];
    const batteryTelemetry = batteryDevices[0]?.latestTelemetry?.data;
    const batterySystem = {
        soc: Number(batteryTelemetry?.soc) || 0,
        chargeRate: Number(batteryTelemetry?.current) || 0,
        temperature: Number(batteryTelemetry?.temperature) || 0,
        remainingCapacity: Number(batteryTelemetry?.energy_today) || 0,
        healthStatus: String(batteryTelemetry?.status) || "Unknown",
        efficiency: Number(batteryTelemetry?.health) || 0,
    };

    // Extract solar array data from site overview
    const solarDevices =
        siteOverview?.devices.filter(
            (d) =>
                d.deviceType === "SOLAR_ARRAY" ||
                d.deviceType === "SOLAR_PANEL" ||
                d.deviceType === "INVERTER"
        ) || [];
    const solarTelemetry = solarDevices[0]?.latestTelemetry?.data;
    const solarArray = {
        currentOutput: Number(solarTelemetry?.power) || 0,
        inverterEfficiency: Number(solarTelemetry?.efficiency) || 0,
        energyYield: Number(solarTelemetry?.energy_today) || 0,
        status: String(solarTelemetry?.status) || "Unknown",
        irradiance: Number(solarTelemetry?.irradiance) || 0,
        panelTemperature: Number(solarTelemetry?.temperature) || 0,
    }; // Extract EV charger data from site overview
    const evDevices =
        siteOverview?.devices.filter((d) => d.deviceType === "EV_CHARGER") ||
        [];
    const evTelemetry = evDevices[0]?.latestTelemetry?.data;
    const evCharger = {
        activeSessions: Number(evTelemetry?.activeSessions) || 0,
        totalSessions: Number(evTelemetry?.totalSessions) || 0,
        powerDelivered: Number(evTelemetry?.powerDelivered) || 0,
        avgSessionDuration: Array.isArray(evTelemetry?.chargerData)
            ? evTelemetry.chargerData.reduce(
                  (acc, curr) => acc + (curr.sessionDuration || 0),
                  0
              ) / evTelemetry.chargerData.length
            : 0,
        revenue: Number(evTelemetry?.revenue) || 0,
        faults: Number(evTelemetry?.faults) || 0,
        uptime: Number(evTelemetry?.uptime) || 0,
    };

    // Mock forecast data (will be integrated into site overview later)
    const forecast = [
        {
            time: "09:00",
            demand: 45,
            generation: 60,
            storage: 75,
            gridImport: 0,
            gridExport: 15,
            batteryLevel: 85,
            temperature: 22,
            cloudCover: 20,
            windSpeed: 5,
            irradiance: 750,
        },
        {
            time: "10:00",
            demand: 48,
            generation: 65,
            storage: 70,
            gridImport: 0,
            gridExport: 17,
            batteryLevel: 80,
            temperature: 24,
            cloudCover: 15,
            windSpeed: 6,
            irradiance: 820,
        },
        {
            time: "11:00",
            demand: 52,
            generation: 70,
            storage: 65,
            gridImport: 0,
            gridExport: 18,
            batteryLevel: 75,
            temperature: 26,
            cloudCover: 10,
            windSpeed: 7,
            irradiance: 900,
        },
        {
            time: "12:00",
            demand: 55,
            generation: 75,
            storage: 60,
            gridImport: 0,
            gridExport: 20,
            batteryLevel: 70,
            temperature: 28,
            cloudCover: 5,
            windSpeed: 8,
            irradiance: 950,
        },
    ];

    // Mock schedule data (will be integrated into site overview later)
    const [schedule, setSchedule] = useState([
        { task: "Battery Charging", time: "08:00" },
        { task: "Grid Export", time: "12:00" },
        { task: "EV Charging", time: "16:00" },
        { task: "System Maintenance", time: "20:00" },
    ]);

    // Use site overview data for site information
    const siteInfo = {
        location: siteOverview?.address || "Unknown Location",
        geo: siteOverview
            ? `${siteOverview.locationLat}° N, ${siteOverview.locationLng}° W`
            : "Unknown Coordinates",
        contact: siteOverview?.contactPerson || "No contact information",
        number: siteOverview?.contactPhone || "No contact number",
        email: siteOverview?.contactEmail || "No email provided",
        website: "www.ecogrid.com", // This field is not yet in the site overview, keeping static for now
    };

    // Real-time data is now handled by WebSocket connection
    // No need for mock data intervals

    const handleDragStart = (
        event: React.DragEvent<HTMLLIElement>,
        task: string
    ) => {
        event.dataTransfer.setData("text/plain", task);
    };

    const handleDrop = (
        event: React.DragEvent<HTMLLIElement>,
        time: string
    ) => {
        const task = event.dataTransfer.getData("text/plain");
        setSchedule((prev) => [...prev, { task, time }]);
        event.preventDefault();
    };

    const handleDragOver = (event: React.DragEvent<HTMLLIElement>) => {
        event.preventDefault();
    };

    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-br from-slate-900 to-slate-800">
            <Topbar selected="EMS" />
            <main className="flex-1 p-8 max-w-7xl mx-auto">
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="text-5xl font-bold text-white mb-2">
                            Energy Management System
                        </h1>
                    </div>
                    <div className="flex items-center gap-4">
                        <div className="h-3 w-3 rounded-full bg-green-500 animate-pulse"></div>
                        <span className="text-green-400">API Connected</span>
                    </div>
                </div>

                {/* Site Selector */}
                <div className="mb-8">
                    <label className="text-white text-lg font-semibold mr-4 block mb-2">
                        Select EMS Site:
                    </label>
                    <SiteSelector
                        sites={sites}
                        selectedSiteId={selectedSiteId}
                        onSiteChange={handleSiteChange}
                    />
                </div>

                {/* 3D Energy Flow Diagram */}
                <section className="mb-8">
                    <div className="p-6 rounded-lg bg-slate-800/50 border border-green-500/20 backdrop-blur">
                        <EnergyFlowDiagram />
                    </div>
                </section>

                {/* Site Information */}
                <section className="mb-8">
                    <h2 className="text-2xl font-bold mb-4">
                        Site Information
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaMapMarkerAlt className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Location
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {siteInfo.location}
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Geo: {siteInfo.geo}
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaPhone className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Contact Information
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {siteInfo.contact}
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Phone: {siteInfo.number}
                            </div>
                            <div className="text-sm text-green-400">
                                Email: {siteInfo.email}
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaGlobe className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Website
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {siteInfo.website}
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Visit for more details
                            </div>
                        </div>
                    </div>
                </section>

                {/* Battery Management System */}
                <section className="mb-8">
                    <h2 className="text-2xl font-bold mb-4">
                        Battery Management System
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaBatteryFull className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        State of Charge (SoC)
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {batterySystem.soc.toFixed(1)} %
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Target band: 80–90 %
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaBolt className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Charge/Discharge Rate
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {batterySystem.chargeRate.toFixed(1)} kW
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                (+) charging / (–) discharging
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaThermometerHalf className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Battery Temperature
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {batterySystem.temperature.toFixed(1)}{" "}
                                        °C
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Avg of 16 modules
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaTag className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Remaining Capacity
                                    </h3>
                                    <p className="text-3xl text-white">
                                        850 kWh
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Nominal: 1 MWh
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaHeartbeat className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Health Status
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {batterySystem.healthStatus}
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Cycles: 1,200 / 5,000
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaSyncAlt className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Round-Trip Efficiency
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {batterySystem.efficiency} %
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                (Discharge ÷ Charge)
                            </div>
                        </div>
                    </div>
                </section>

                {/* Solar Array */}
                <section className="mb-8">
                    <h2 className="text-2xl font-bold mb-4">Solar Array</h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaSun className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Current Output
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {solarArray.currentOutput.toFixed(1)} kW
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Peak today at 14:00
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaChartLine className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Today’s Energy Yield
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {solarArray.energyYield.toFixed(1)} kWh
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                +5% vs. yesterday
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaThermometerHalf className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Panel Temperature
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {solarArray.panelTemperature.toFixed(1)}{" "}
                                        °C
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Safe operating (&lt; 60 °C)
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaCloudSun className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Irradiance
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {solarArray.irradiance} W/m²
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Cloud cover: 30 %
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaTools className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Inverter Efficiency
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {solarArray.inverterEfficiency} %
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Model: SMA Sunny Boy 5.0
                            </div>
                        </div>
                    </div>
                </section>

                {/* EV Charger Station */}
                <section className="mb-8">
                    <h2 className="text-2xl font-bold mb-4">
                        EV Charger Station
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaPlug className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Active Sessions
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {evCharger.activeSessions} / {evCharger.totalSessions}
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                7 ports available
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaBolt className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Power Delivered (Today)
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {evCharger.powerDelivered.toFixed(1)}{" "}
                                        kWh
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Avg per session: 40 kWh
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaClock className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Avg Session Duration
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {evCharger.avgSessionDuration} min
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Peak hours: 17:00–19:00
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaDollarSign className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Revenue (Today)
                                    </h3>
                                    <p className="text-3xl text-white">
                                        ${evCharger.revenue.toFixed(2)}
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Rate: $0.15/kWh
                            </div>
                        </div>
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                            <div className="flex items-center gap-4 mb-4">
                                <div className="p-3 bg-green-500/10 rounded-lg">
                                    <FaExclamationTriangle className="w-6 h-6 text-green-400" />
                                </div>
                                <div>
                                    <h3 className="text-green-400 text-sm font-semibold">
                                        Faults / Alerts
                                    </h3>
                                    <p className="text-3xl text-white">
                                        {evCharger.faults}
                                    </p>
                                </div>
                            </div>
                            <div className="text-sm text-green-400">
                                Uptime: {evCharger.uptime} %
                            </div>
                        </div>
                    </div>
                </section>

                {/* Forecast & Scheduler */}
                <section className="mb-8">
                    <h2 className="text-2xl font-bold mb-4">
                        Forecast & Scheduler
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* Forecast */}
                        <div className="bg-slate-800/50 p-6 rounded-lg border border-green-500/20 backdrop-blur">
                            <h3 className="text-lg font-semibold text-green-400 mb-4">
                                Solar Forecast
                            </h3>
                            <ul>
                                {forecast.map((entry) => (
                                    <li
                                        key={entry.time}
                                        className="flex justify-between text-white mb-2"
                                    >
                                        <span>{entry.time}</span>
                                        <span>{entry.irradiance} W/m²</span>
                                    </li>
                                ))}
                            </ul>
                        </div>

                        {/* Scheduler */}
                        <div className="bg-slate-800/50 p-6 rounded-lg border border-green-500/20 backdrop-blur">
                            <h3 className="text-lg font-semibold text-green-400 mb-4">
                                Scheduler
                            </h3>
                            <div className="mb-4">
                                <h4 className="text-sm text-green-400 mb-2">
                                    Tasks
                                </h4>
                                <ul>
                                    {[
                                        "Battery Charging",
                                        "Grid Export",
                                        "EV Charging",
                                    ].map((task) => (
                                        <li
                                            key={task}
                                            draggable
                                            onDragStart={(e) =>
                                                handleDragStart(e, task)
                                            }
                                            className="cursor-pointer text-white mb-2"
                                        >
                                            {task}
                                        </li>
                                    ))}
                                </ul>
                            </div>
                            <div>
                                <h4 className="text-sm text-green-400 mb-2">
                                    Schedule
                                </h4>
                                <ul>
                                    {forecast.map((entry) => (
                                        <li
                                            key={entry.time}
                                            onDrop={(e) =>
                                                handleDrop(e, entry.time)
                                            }
                                            onDragOver={handleDragOver}
                                            className="p-2 border border-dashed border-green-500 rounded mb-2 text-white flex items-center gap-4"
                                        >
                                            <span className="min-w-[60px]">
                                                {entry.time}
                                            </span>
                                            <ul className="flex gap-2 ml-2">
                                                {schedule
                                                    .filter(
                                                        (s) =>
                                                            s.time ===
                                                            entry.time
                                                    )
                                                    .map((s, index) => (
                                                        <li
                                                            key={index}
                                                            className="bg-green-500/20 px-2 py-1 rounded"
                                                        >
                                                            {s.task}
                                                        </li>
                                                    ))}
                                            </ul>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                    </div>
                </section>

                {/* Alerts & Controls */}
                <section>
                    <h2 className="text-2xl font-bold mb-4">
                        Alerts & Controls
                    </h2>
                    <div className="bg-slate-800/50 p-6 rounded-lg border border-green-500/20 backdrop-blur">
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                            {/* One-click Controls */}
                            <div>
                                <h3 className="text-lg font-semibold text-green-400 mb-4">
                                    One-Click Controls
                                </h3>
                                <div className="flex flex-col gap-4 mt-12">
                                    <button
                                        className="bg-green-500 text-white py-2 px-4 rounded hover:bg-green-600"
                                        onClick={() =>
                                            alert("Battery charging started!")
                                        }
                                    >
                                        Start Battery Charging
                                    </button>
                                    <button
                                        className="bg-green-500 text-white py-2 px-4 rounded hover:bg-green-600"
                                        onClick={() =>
                                            alert("Grid export initiated!")
                                        }
                                    >
                                        Initiate Grid Export
                                    </button>
                                    <button
                                        className="bg-green-500 text-white py-2 px-4 rounded hover:bg-green-600"
                                        onClick={() =>
                                            alert("EV charging enabled!")
                                        }
                                    >
                                        Enable EV Charging
                                    </button>
                                </div>
                            </div>

                            {/* Threshold Alarms */}
                            <div>
                                <h3 className="text-lg font-semibold text-green-400 mb-4">
                                    Threshold Alarms
                                </h3>
                                <div className="grid grid-cols-1 gap-4">
                                    <div>
                                        <label className="text-white block mb-2">
                                            Battery SOC Threshold (%)
                                        </label>
                                        <input
                                            type="number"
                                            className="w-full p-2 rounded bg-slate-700 text-white border border-green-500"
                                            onBlur={(e) =>
                                                alert(
                                                    `Battery SOC threshold set to ${e.target.value}%`
                                                )
                                            }
                                        />
                                    </div>
                                    <div>
                                        <label className="text-white block mb-2">
                                            Solar Output Threshold (kW)
                                        </label>
                                        <input
                                            type="number"
                                            className="w-full p-2 rounded bg-slate-700 text-white border border-green-500"
                                            onBlur={(e) =>
                                                alert(
                                                    `Solar output threshold set to ${e.target.value} kW`
                                                )
                                            }
                                        />
                                    </div>
                                    <div>
                                        <label className="text-white block mb-2">
                                            EV Charger Fault Threshold
                                        </label>
                                        <input
                                            type="number"
                                            className="w-full p-2 rounded bg-slate-700 text-white border border-green-500"
                                            onBlur={(e) =>
                                                alert(
                                                    `EV charger fault threshold set to ${e.target.value}`
                                                )
                                            }
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
};

const EMSPage: React.FC = () => {
    return (
        <Suspense
            fallback={
                <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 to-slate-800">
                    <div className="text-center">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500 mx-auto mb-4"></div>
                        <p className="text-white">Loading...</p>
                    </div>
                </div>
            }
        >
            <EMSPageContent />
        </Suspense>
    );
};

export default EMSPage;
