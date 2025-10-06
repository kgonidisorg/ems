"use client";

import { useState, useEffect, useCallback } from "react";
import MapView from "@/components/MapView";
import Topbar from "@/components/Topbar";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { useAuth } from "@/contexts/AuthContext";
import { AnalyticsService, SiteService } from "@/lib/api";
// import { useWebSocket } from "@/lib/websocket"; // TODO: Re-enable when backend WebSocket is implemented
import { useThrottle } from "@/hooks/useDebounce";
import type { DashboardResponse, Site } from "@/lib/types";

export default function Home() {
    const { isAuthenticated, isLoading: authLoading } = useAuth();
    
    // Real-time dashboard data
    const [dashboardData, setDashboardData] =
        useState<DashboardResponse | null>(null);
    const [sites, setSites] = useState<Site[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [lastUpdate] = useState<Date>(new Date());

    // TODO: WebSocket for real-time updates (temporarily disabled until backend WebSocket is implemented)
    // const { connect, disconnect, on, off, isConnected } = useWebSocket();

    // Fetch initial dashboard data
    const fetchDashboardData = useCallback(async () => {
        try {
            setError(null);
            const [dashboard, sitesData] = await Promise.all([
                AnalyticsService.getDashboard({ hoursBack: 24 }),
                SiteService.getSites({ size: 50 }),
            ]);

            setDashboardData(dashboard);
            setSites(sitesData);
        } catch (err) {
            console.error("Failed to fetch dashboard data:", err);
            setError("Failed to load dashboard data. Please try again.");
        } finally {
            setLoading(false);
        }
    }, []);

    // Throttled version to prevent spam
    const throttledFetchDashboardData = useThrottle(fetchDashboardData, 5000); // 5 seconds minimum between calls

    // TODO: WebSocket message handler (temporarily disabled)
    // const handleWebSocketMessage = (message: unknown) => {
    //     const msg = message as { type: string; data: DashboardResponse | Site };
    //     if (msg?.type === 'DASHBOARD_UPDATE') {
    //         setDashboardData(msg.data as DashboardResponse);
    //         setLastUpdate(new Date());
    //     } else if (msg?.type === 'SITE_UPDATE') {
    //         setSites(prev => {
    //             const updated = [...prev];
    //             const siteData = msg.data as Site;
    //             const index = updated.findIndex(site => site.id === siteData.id);
    //             if (index >= 0) {
    //                 updated[index] = { ...updated[index], ...siteData };
    //             }
    //             return updated;
    //         });
    //     }
    // };

    useEffect(() => {
        // Only fetch data if user is authenticated and auth loading is complete
        // This prevents API calls from happening before user is properly authenticated
        // ProtectedRoute will redirect unauthenticated users to login
        if (!authLoading && isAuthenticated) {
            fetchDashboardData();

            // TODO: Set up WebSocket connection once backend WebSocket server is implemented
            // connect();
            // on('message', handleWebSocketMessage);

            // Fallback: refresh data every 5 minutes (since WebSocket is temporarily disabled)
            const fallbackInterval = setInterval(() => {
                throttledFetchDashboardData();
            }, 300000); // 5 minutes

            return () => {
                // off('message', handleWebSocketMessage);
                // disconnect();
                clearInterval(fallbackInterval);
            };
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [authLoading, isAuthenticated]); // Run when auth state changes

    // Loading state
    if (loading) {
        return (
            <ProtectedRoute>
                <div className="flex min-h-screen flex-col bg-gradient-to-br from-slate-900 to-slate-800">
                    <Topbar />
                    <main className="flex-1 flex items-center justify-center">
                        <div className="text-center">
                            <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-green-500 mx-auto mb-4"></div>
                            <p className="text-white text-lg">
                                Loading dashboard data...
                            </p>
                        </div>
                    </main>
                </div>
            </ProtectedRoute>
        );
    }

    // Error state
    if (error) {
        return (
            <ProtectedRoute>
                <div className="flex min-h-screen flex-col bg-gradient-to-br from-slate-900 to-slate-800">
                    <Topbar />
                    <main className="flex-1 flex items-center justify-center">
                        <div className="text-center">
                            <div className="text-red-500 text-6xl mb-4">⚠️</div>
                            <h2 className="text-white text-2xl mb-4">
                                Error Loading Dashboard
                            </h2>
                            <p className="text-red-400 mb-6">{error}</p>
                            <button
                                onClick={() => {
                                    setLoading(true);
                                    setError(null);
                                    fetchDashboardData();
                                }}
                                className="bg-green-500 hover:bg-green-600 text-white px-6 py-2 rounded-lg transition-colors"
                            >
                                Retry
                            </button>
                        </div>
                    </main>
                </div>
            </ProtectedRoute>
        );
    }

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
                                    <div className="h-3 w-3 rounded-full bg-blue-500"></div>
                                    <span className="text-blue-400">
                                        Network Status (Static Mode)
                                    </span>
                                    {lastUpdate && (
                                        <span className="text-slate-400 text-sm">
                                            Last update:{" "}
                                            {lastUpdate.toLocaleTimeString()}
                                        </span>
                                    )}
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
                                                {dashboardData
                                                    ? (
                                                          dashboardData.totalEnergyGenerated +
                                                          dashboardData.totalEnergyConsumed
                                                      ).toLocaleString()
                                                    : "0"}{" "}
                                                MW
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
                                                {dashboardData
                                                    ? (
                                                          dashboardData.carbonFootprintReduced /
                                                          1000
                                                      ).toFixed(1)
                                                    : "0"}
                                                k tons
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
                                                {dashboardData
                                                    ? (
                                                          dashboardData.costSavings /
                                                          1000000
                                                      ).toFixed(1)
                                                    : "0"}
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
                                                {dashboardData?.activeSites ||
                                                    0}
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
                                                {dashboardData?.activeDevices ||
                                                    0}
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
                                                        width: `${
                                                            dashboardData?.averageEfficiency ||
                                                            0
                                                        }%`,
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
                                                        {dashboardData?.averageEfficiency.toFixed(
                                                            1
                                                        ) || "0"}
                                                        %
                                                    </span>
                                                </div>
                                                <div className="flex items-center gap-2">
                                                    <div className="h-3 w-3 bg-slate-500 rounded"></div>
                                                    <span className="text-white">
                                                        Grid Power
                                                    </span>
                                                    <span className="text-slate-400 ml-auto">
                                                        {dashboardData
                                                            ? (
                                                                  100 -
                                                                  dashboardData.averageEfficiency
                                                              ).toFixed(1)
                                                            : "100"}
                                                        %
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
