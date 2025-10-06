"use client";
import React, { useState } from "react";
import EnergyFlowDiagram from "@/components/EnergyFlowDiagram";
import Topbar from "@/components/Topbar";
import { useEMSWebSocket } from "@/hooks/useEMSWebSocket";
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

const EMSPage: React.FC = () => {
    const [selectedSite, setSelectedSite] = useState<string>("New York");
    
    // WebSocket connection for real-time data - use siteId based on selectedSite
    const siteId = selectedSite.toLowerCase().replace(' ', '-');
    const { 
        data: emsData, 
        isConnected, 
        hasError 
    } = useEMSWebSocket(siteId);

    // Extract battery system data from WebSocket with fallbacks
    const batterySystem = emsData?.batterySystem || {
        soc: 0,
        chargeRate: 0,
        temperature: 0,
        remainingCapacity: 0,
        healthStatus: "Unknown",
        efficiency: 0,
    };

    // Extract solar array data from WebSocket with fallbacks
    const solarArray = emsData?.solarArray || {
        currentOutput: 0,
        energyYield: 0,
        panelTemperature: 0,
        irradiance: 0,
        inverterEfficiency: 0,
    };

    // Extract EV charger data from WebSocket with fallbacks
    const evCharger = emsData?.evCharger || {
        activeSessions: 0,
        powerDelivered: 0,
        avgSessionDuration: 0,
        revenue: 0,
        faults: 0,
        uptime: 0,
    };

    // Extract forecast data from WebSocket with fallbacks
    const forecast = emsData.forecast?.map((item) => ({
        time: item.time,
        irradiance: item.irradiance || 0
    })) || [
        { time: "08:00", irradiance: 0 },
        { time: "10:00", irradiance: 0 },
        { time: "12:00", irradiance: 0 },
        { time: "14:00", irradiance: 0 },
        { time: "16:00", irradiance: 0 },
        { time: "18:00", irradiance: 0 },
    ];

    // Extract schedule data from WebSocket with fallbacks
    const [schedule, setSchedule] = useState(
        emsData.schedule?.map(item => ({
            task: item.task || "Unknown",
            time: item.time || "00:00"
        })) || [
            { task: "Battery Charging", time: "08:00" },
            { task: "Grid Export", time: "12:00" },
            { task: "EV Charging", time: "16:00" },
        ]
    );

    const sites: Record<
        string,
        {
            location: string;
            geo: string;
            contact: string;
            email: string;
            website: string;
        }
    > = {
        "New York": {
            location: "New York, USA",
            geo: "40.7128° N, 74.0060° W",
            contact: "+1 (555) 123-4567",
            email: "contact@ecogrid.com",
            website: "www.ecogrid.com",
        },
        "Los Angeles": {
            location: "Los Angeles, USA",
            geo: "34.0522° N, 118.2437° W",
            contact: "+1 (555) 987-6543",
            email: "la@ecogrid.com",
            website: "www.ecogrid-la.com",
        },
        Chicago: {
            location: "Chicago, USA",
            geo: "41.8781° N, 87.6298° W",
            contact: "+1 (555) 456-7890",
            email: "chicago@ecogrid.com",
            website: "www.ecogrid-chicago.com",
        },
        Houston: {
            location: "Houston, USA",
            geo: "29.7604° N, 95.3698° W",
            contact: "+1 (555) 321-6549",
            email: "houston@ecogrid.com",
            website: "www.ecogrid-houston.com",
        },
        Miami: {
            location: "Miami, USA",
            geo: "25.7617° N, 80.1918° W",
            contact: "+1 (555) 654-3210",
            email: "miami@ecogrid.com",
            website: "www.ecogrid-miami.com",
        },
    };

    // Use WebSocket site info if available, otherwise fallback to static data
    const siteInfo = emsData.siteInfo ? {
        location: emsData.siteInfo.location || sites[selectedSite].location,
        geo: emsData.siteInfo.geo || sites[selectedSite].geo,
        contact: emsData.siteInfo.contact || sites[selectedSite].contact,
        email: emsData.siteInfo.email || sites[selectedSite].email,
        website: emsData.siteInfo.website || sites[selectedSite].website,
    } : sites[selectedSite];

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
                        <div className={`h-3 w-3 rounded-full ${
                            isConnected 
                                ? 'bg-green-500 animate-pulse' 
                                : hasError 
                                    ? 'bg-red-500' 
                                    : 'bg-yellow-500 animate-pulse'
                        }`}></div>
                        <span className={`${
                            isConnected 
                                ? 'text-green-400' 
                                : hasError 
                                    ? 'text-red-400' 
                                    : 'text-yellow-400'
                        }`}>
                            {isConnected 
                                ? 'WebSocket Connected' 
                                : hasError 
                                    ? 'Connection Error' 
                                    : 'Connecting...'}
                        </span>
                    </div>
                </div>

                {/* Site Selector */}
                <div className="mb-8">
                    <label
                        htmlFor="site-selector"
                        className="text-white text-lg font-semibold mr-4"
                    >
                        Select EMS Site:
                    </label>
                    <select
                        id="site-selector"
                        className="p-2 px-5 rounded bg-slate-800 text-white border border-green-500"
                        value={selectedSite}
                        onChange={(e) => setSelectedSite(e.target.value)}
                    >
                        {Object.keys(sites).map((site) => (
                            <option key={site} value={site}>
                                {site}
                            </option>
                        ))}
                    </select>
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
                                        {evCharger.activeSessions} / 10
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

export default EMSPage;
