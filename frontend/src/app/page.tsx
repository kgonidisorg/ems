"use client";

import { useState, useEffect } from "react";
import MapView from "@/components/MapView";
import Topbar from "@/components/Topbar";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";

export default function Home() {
    const [totalSites, setTotalSites] = useState(342);
    const [totalCapacity, setTotalCapacity] = useState(2450);
    const [carbonSaved, setCarbonSaved] = useState(28500);
    const [gridRevenue, setGridRevenue] = useState(1540000);
    const [renewableMix, setRenewableMix] = useState(78);
    const [activeServices, setActiveServices] = useState(289);
    const [sites, setSites] = useState<
        { lat: number; lng: number; name: string }[]
    >([]);

    // Simulate real-time updates
    useEffect(() => {
        const interval = setInterval(() => {
            setCarbonSaved((prev) => prev + Math.random() * 10);
            setGridRevenue((prev) => prev + Math.random() * 0.5);
        }, 3000);
        // Generate sites only on land (roughly within US mainland bounding box, avoiding obvious water areas)
        const isLand = (lat: number, lng: number) => {
            // Roughly exclude Gulf of Mexico, Atlantic, and Pacific by bounding box
            // This is not perfect, but avoids most water
            if (lng < -124 || lng > -66) return false; // outside continental US
            if (lat < 25 || lat > 49) return false; // outside continental US
            // Exclude obvious ocean areas (very rough)
            if (lng < -117 && lat < 38) return false; // Pacific SW
            if (lng < -90 && lat < 30) return false; // Gulf of Mexico
            if (lng > -79 && lat < 40) return false; // Atlantic SE
            return true;
        };

        const generatedSites = [];
        while (generatedSites.length < 50) {
            const lat = 25 + Math.random() * 24; // 25 to 49
            const lng = -124 + Math.random() * 58; // -124 to -66
            if (isLand(lat, lng)) {
                generatedSites.push({
                    lat,
                    lng,
                    name: `Site ${generatedSites.length + 1}`,
                });
            }
        }
        setSites(generatedSites);

        return () => clearInterval(interval);
    }, []);

    return (
        <ProtectedRoute>
            <div className="flex min-h-screen flex-col bg-gradient-to-br from-slate-900 to-slate-800">
                <Topbar />
                <main className="flex-1">
                    <div className="p-8">
                        <div className="max-w-7xl mx-auto">
                        {/* Header */}
                        <div className="flex items-center justify-between mb-8">
                            <div>
                                <h1 className="text-5xl font-bold text-white mb-2">
                                    EcoGrid Network
                                </h1>
                                <p className="text-green-400">
                                    North America&apos;s Leading Energy
                                    Management Platform
                                </p>
                            </div>
                            <div className="flex items-center gap-4">
                                <div className="h-3 w-3 bg-green-500 rounded-full animate-pulse"></div>
                                <span className="text-green-400">
                                    Live Network Status
                                </span>
                            </div>
                        </div>

                        {/* Map Section */}
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur mb-8">
                            <h2 className="text-2xl font-bold text-white mb-6">
                                Nationwide Map
                            </h2>
                            <div className="h-[500px] w-full">
                                <MapView sites={sites} />
                            </div>
                        </div>

                        {/* Executive Overview */}
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="p-3 bg-green-500/10 rounded-lg">
                                        <svg
                                            className="w-6 h-6 text-green-400"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                            stroke="currentColor"
                                        >
                                            <path
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                                strokeWidth={2}
                                                d="M13 10V3L4 14h7v7l9-11h-7z"
                                            />
                                        </svg>
                                    </div>
                                    <div>
                                        <h3 className="text-green-400 text-sm font-semibold">
                                            Network Capacity
                                        </h3>
                                        <p className="text-3xl text-white">
                                            {totalCapacity.toLocaleString()} MW
                                        </p>
                                    </div>
                                </div>
                                <div className="h-1 w-full bg-slate-700 rounded">
                                    <div
                                        className="h-1 bg-green-400 rounded"
                                        style={{ width: "85%" }}
                                    ></div>
                                </div>
                            </div>

                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="p-3 bg-green-500/10 rounded-lg">
                                        <svg
                                            className="w-6 h-6 text-green-400"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                            stroke="currentColor"
                                        >
                                            <path
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                                strokeWidth={2}
                                                d="M3 6l3 1m0 0l-3 9a5.002 5.002 0 006.001 0M6 7l3 9M6 7l6-2m6 2l3-1m-3 1l-3 9a5.002 5.002 0 006.001 0M18 7l3 9m-3-9l-6-2m0-2v2m0 16V5m0 16H9m3 0h3"
                                            />
                                        </svg>
                                    </div>
                                    <div>
                                        <h3 className="text-green-400 text-sm font-semibold">
                                            Carbon Offset
                                        </h3>
                                        <p className="text-3xl text-white">
                                            {(carbonSaved / 1000).toFixed(1)}k
                                            tons
                                        </p>
                                    </div>
                                </div>
                                <div className="flex justify-between text-sm">
                                    <span className="text-green-400">
                                        YTD Target
                                    </span>
                                    <span className="text-white">
                                        92% achieved
                                    </span>
                                </div>
                            </div>

                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="p-3 bg-green-500/10 rounded-lg">
                                        <svg
                                            className="w-6 h-6 text-green-400"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                            stroke="currentColor"
                                        >
                                            <path
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                                strokeWidth={2}
                                                d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                                            />
                                        </svg>
                                    </div>
                                    <div>
                                        <h3 className="text-green-400 text-sm font-semibold">
                                            Revenue
                                        </h3>
                                        <p className="text-3xl text-white">
                                            $
                                            {(gridRevenue / 1000000).toFixed(1)}
                                            M
                                        </p>
                                    </div>
                                </div>
                                <div className="text-sm text-green-400">
                                    +12.5% from last month
                                </div>
                            </div>
                        </div>

                        {/* Real-Time Monitoring */}
                        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <h2 className="text-2xl font-bold text-white mb-6">
                                    Network Status
                                </h2>
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="p-4 bg-slate-700/50 rounded-lg">
                                        <h3 className="text-green-400 text-sm mb-2">
                                            Active Sites
                                        </h3>
                                        <p className="text-2xl text-white">
                                            {totalSites}
                                        </p>
                                        <div className="flex items-center gap-2 mt-2">
                                            <div className="h-2 w-2 bg-green-500 rounded-full"></div>
                                            <span className="text-sm text-green-400">
                                                98% Online
                                            </span>
                                        </div>
                                    </div>
                                    <div className="p-4 bg-slate-700/50 rounded-lg">
                                        <h3 className="text-green-400 text-sm mb-2">
                                            Grid Services
                                        </h3>
                                        <p className="text-2xl text-white">
                                            {activeServices}
                                        </p>
                                        <div className="flex items-center gap-2 mt-2">
                                            <div className="h-2 w-2 bg-yellow-500 rounded-full"></div>
                                            <span className="text-sm text-yellow-400">
                                                12 Pending
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <h2 className="text-2xl font-bold text-white mb-6">
                                    Renewable Mix
                                </h2>
                                <div className="flex items-center justify-between">
                                    <div className="flex-1">
                                        <div className="h-4 w-full bg-slate-700 rounded-full overflow-hidden">
                                            <div
                                                className="h-full bg-gradient-to-r from-green-500 to-green-400"
                                                style={{
                                                    width: `${renewableMix}%`,
                                                }}
                                            ></div>
                                        </div>
                                        <div className="mt-4 space-y-2">
                                            <div className="flex items-center gap-2">
                                                <div className="h-3 w-3 bg-green-500 rounded"></div>
                                                <span className="text-white">
                                                    Solar + Storage
                                                </span>
                                                <span className="text-green-400 ml-auto">
                                                    {renewableMix}%
                                                </span>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <div className="h-3 w-3 bg-slate-500 rounded"></div>
                                                <span className="text-white">
                                                    Grid Power
                                                </span>
                                                <span className="text-slate-400 ml-auto">
                                                    {100 - renewableMix}%
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Sustainability Impact */}
                        <div className="bg-slate-700 p-6 rounded-lg mb-8">
                            <h2 className="text-2xl font-bold text-green-400 mb-4">
                                Sustainability Impact
                            </h2>
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <div className="text-center">
                                    <p className="text-4xl text-white font-bold">
                                        24.5k
                                    </p>
                                    <p className="text-green-400">
                                        Trees Equivalent
                                    </p>
                                </div>
                                <div className="text-center">
                                    <p className="text-4xl text-white font-bold">
                                        85%
                                    </p>
                                    <p className="text-green-400">
                                        Grid-Renewable Mix
                                    </p>
                                </div>
                                <div className="text-center">
                                    <p className="text-4xl text-white font-bold">
                                        $128k
                                    </p>
                                    <p className="text-green-400">
                                        Carbon Credits
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
        </ProtectedRoute>
    );
}
