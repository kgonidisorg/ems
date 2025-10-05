"use client";

import React, { useState, useEffect } from "react";
import {
    FaBatteryHalf,
    FaBolt,
    FaSun,
    FaPlug,
    FaTable,
    FaShareAlt,
    FaTimes,
    FaClock,
    FaBell,
} from "react-icons/fa";
// import dynamic from "next/dynamic";
import Modal from "react-modal";
import Topbar from "@/components/Topbar";
import { deviceAPI } from "@/lib/analytics";
import { useAsyncData } from "@/hooks/useAsyncData";
import {
    LoadingSpinner,
    ErrorDisplay,
} from "@/components/ui/LoadingComponents";
import type { Site } from "@/lib/types";

import dynamic from "next/dynamic";
const ClusterMapView = dynamic(() => import("@/components/ClusterMapView"), {
    ssr: false,
});

type Cluster = {
    name: string;
    lat: number;
    lng: number;
    avgSOC: number;
    evThroughput: number;
    solarYield: number;
    gridReliance: number;
    revenueBreakdown: {
        ancillary: number;
        demandResponse: number;
        arbitrage: number;
    };
    carbonOffset: number;
    batteryHealth: number;
    uptime: number;
    alarms: {
        critical: number;
        warning: number;
        info: number;
    };
    numberOfSystems: number; // Optional field for number of systems
};

const ClusterDashboard: React.FC = () => {
    // Fetch real sites data
    const {
        data: sites,
        loading,
        error,
        refetch,
    } = useAsyncData(() => deviceAPI.getSites({ size: 100 }));

    // Transform sites into clusters with additional mock data for demonstration
    const clusters: Cluster[] = React.useMemo(() => {
        if (!sites) return [];
        return sites.map((site) => ({
            name: site.name,
            lat: site.locationLat,
            lng: site.locationLng,
            avgSOC: 75 + Math.random() * 20, // Mock data
            evThroughput: 90 + Math.random() * 40, // Mock data
            solarYield: 400 + Math.random() * 200, // Mock data
            gridReliance: 25 + Math.random() * 25, // Mock data
            revenueBreakdown: {
                ancillary: 150 + Math.random() * 100,
                demandResponse: 100 + Math.random() * 80,
                arbitrage: 70 + Math.random() * 60,
            },
            carbonOffset: 200 + Math.random() * 100, // Mock data
            batteryHealth: 85 + Math.random() * 10, // Mock data
            uptime: 98 + Math.random() * 2, // Mock data
            alarms: {
                critical: Math.floor(Math.random() * 3),
                warning: Math.floor(Math.random() * 5),
                info: Math.floor(Math.random() * 8),
            },
            numberOfSystems:
                site.devices?.length || 30 + Math.floor(Math.random() * 30),
        }));
    }, [sites]);

    const [selectedCluster, setSelectedCluster] = useState<Cluster | null>(
        null
    );
    const [isModalOpen, setIsModalOpen] = useState(false);
    // Pagination state for the statistics table
    const [pageSize, setPageSize] = useState<number>(10);
    const [page, setPage] = useState<number>(1);

    const openModal = React.useCallback((cluster: Cluster) => {
        setSelectedCluster(cluster);
        setIsModalOpen(true);
    }, []);

    const closeModal = () => {
        setSelectedCluster(null);
        setIsModalOpen(false);
    };

    useEffect(() => {
        Modal.setAppElement("#__next");
    }, []);

    // Memoize sites passed to ClusterMapView including stable handlers
    const mapSites = React.useMemo(() => {
        return clusters.map((c) => ({
            lat: c.lat,
            lng: c.lng,
            name: c.name,
            soc: Math.round(c.avgSOC),
            onClick: () => openModal(c),
        }));
    }, [clusters, openModal]);

    // Pagination-derived values
    const totalClusters = clusters.length;
    const totalPages = Math.max(1, Math.ceil(totalClusters / pageSize));

    // Ensure current page is within bounds when totalPages changes
    useEffect(() => {
        if (page > totalPages) setPage(totalPages);
        if (page < 1) setPage(1);
    }, [page, totalPages]);

    const paginatedClusters = clusters.slice(
        (page - 1) * pageSize,
        page * pageSize
    );

    // Loading state
    if (loading) {
        return (
            <div className="flex min-h-screen flex-col bg-gradient-to-br from-slate-900 to-slate-800">
                <Topbar selected="Network" />
                <main className="flex-1 flex items-center justify-center">
                    <LoadingSpinner
                        size="lg"
                        message="Loading network data..."
                    />
                </main>
            </div>
        );
    }

    // Error state
    if (error) {
        return (
            <div className="flex min-h-screen flex-col bg-gradient-to-br from-slate-900 to-slate-800">
                <Topbar selected="Network" />
                <main className="flex-1 flex items-center justify-center">
                    <ErrorDisplay error={error} onRetry={refetch} />
                </main>
            </div>
        );
    }

    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-br from-slate-900 to-slate-800">
            <Topbar selected="Network" />
            <main className="flex-1">
                <div className="p-8">
                    <div className="max-w-7xl mx-auto">
                        <h1 className="text-5xl font-bold text-white mb-8">
                            EcoGrid Active Deployments
                        </h1>

                        {/* Map Section */}
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur mb-8">
                            <h2 className="text-2xl font-bold text-white mb-6">
                                Density Heatmap
                            </h2>
                            <div className="h-[500px] w-full bg-slate-700 rounded-lg">
                                {clusters.length > 0 ? (
                                    <ClusterMapView sites={mapSites} />
                                ) : (
                                    <div className="h-full w-full flex items-center justify-center">
                                        <div className="text-center">
                                            <div className="text-green-400 text-6xl mb-4">
                                                🗺️
                                            </div>
                                            <h3 className="text-white text-xl mb-2">
                                                Network Map
                                            </h3>
                                            <p className="text-slate-400">
                                                No sites available
                                            </p>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Collaboration Section */}
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur mb-8">
                            <h2 className="text-2xl font-bold text-white mb-6">
                                Collaboration & Sharing
                            </h2>
                            <div className="flex gap-4">
                                <button className="bg-green-500 text-white px-4 py-2 rounded-lg flex items-center gap-2">
                                    <FaShareAlt /> Share View
                                </button>
                                <button className="bg-blue-500 text-white px-4 py-2 rounded-lg flex items-center gap-2">
                                    <FaTable /> Export Table
                                </button>
                            </div>
                        </div>

                        {/* Network Statistics Table */}
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur mb-8">
                            <h2 className="text-2xl font-bold text-white mb-6">
                                Network Statistics
                            </h2>

                            <div className="flex items-center justify-between mb-4">
                                <div className="flex items-center gap-2">
                                    <label className="text-slate-300">Rows:</label>
                                    <select
                                        value={pageSize}
                                        onChange={(e) => {
                                            const val = Number(e.target.value);
                                            setPageSize(val);
                                            setPage(1);
                                        }}
                                        className="bg-slate-700 text-white p-1 rounded"
                                    >
                                        <option value={10}>10</option>
                                        <option value={25}>25</option>
                                        <option value={100}>100</option>
                                    </select>
                                </div>

                                <div className="flex items-center gap-2 text-slate-300">
                                    <button
                                        onClick={() => setPage((p) => Math.max(1, p - 1))}
                                        disabled={page === 1}
                                        className="px-3 py-1 bg-slate-700 text-white rounded disabled:opacity-50"
                                    >
                                        Prev
                                    </button>
                                    <span>
                                        Page {page} of {totalPages}
                                    </span>
                                    <button
                                        onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                                        disabled={page === totalPages}
                                        className="px-3 py-1 bg-slate-700 text-white rounded disabled:opacity-50"
                                    >
                                        Next
                                    </button>
                                </div>
                            </div>

                            <table className="w-full text-left bg-slate-800 rounded-lg">
                                <thead>
                                    <tr className="bg-slate-700">
                                        <th className="p-4">Cluster</th>
                                        <th className="p-4">Avg. SOC</th>
                                        <th className="p-4">EV Throughput</th>
                                        <th className="p-4">Solar Yield</th>
                                        <th className="p-4">Grid Reliance</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {paginatedClusters.map((cluster) => (
                                        <tr
                                            key={cluster.name}
                                            className="border-t border-slate-700"
                                        >
                                            <td className="p-4">
                                                {cluster.name}
                                            </td>
                                            <td className="p-4">
                                                {Math.round(cluster.avgSOC)}%
                                            </td>
                                            <td className="p-4">
                                                {Math.round(cluster.evThroughput)} kWh/hr
                                            </td>
                                            <td className="p-4">
                                                {Math.round(cluster.solarYield)} kWh/day
                                            </td>
                                            <td className="p-4">
                                                {Math.round(cluster.gridReliance)}%
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </main>

            {/* Cluster Details Modal */}
            <Modal
                isOpen={isModalOpen}
                onRequestClose={closeModal}
                contentLabel="Cluster Details"
                overlayClassName="fixed inset-0 bg-gray-600/50 flex justify-center items-center"
                className="relative bg-slate-800 p-6 rounded-lg shadow-lg border border-green-500/20"
            >
                {selectedCluster && (
                    <div>
                        <button
                            onClick={closeModal}
                            className="absolute top-4 right-4 text-white text-2xl"
                        >
                            <FaTimes />
                        </button>
                        <h2 className="text-2xl font-bold mb-4">
                            {selectedCluster.name}
                        </h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="p-3 bg-green-500/10 rounded-lg">
                                        <FaBatteryHalf className="w-6 h-6 text-green-400" />
                                    </div>
                                    <div>
                                        <h3 className="text-green-400 text-sm font-semibold">
                                            Avg. SOC
                                        </h3>
                                        <p className="text-3xl text-white">
                                            {Math.round(selectedCluster.avgSOC)}%
                                        </p>
                                    </div>
                                </div>
                                <div className="h-1 w-full bg-slate-700 rounded">
                                        <div
                                        className="h-1 bg-green-400 rounded"
                                        style={{
                                            width: `${Math.round(selectedCluster.avgSOC)}%`,
                                        }}
                                    ></div>
                                </div>
                            </div>

                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="p-3 bg-green-500/10 rounded-lg">
                                        <FaPlug className="w-6 h-6 text-yellow-400" />
                                    </div>
                                    <div>
                                        <h3 className="text-green-400 text-sm font-semibold">
                                            EV Throughput
                                        </h3>
                                        <p className="text-3xl text-white">
                                            {Math.round(selectedCluster.evThroughput)} kWh/hr
                                        </p>
                                    </div>
                                </div>
                                <p className="text-sm text-yellow-400">
                                    Can handle up to 4 EVs simultaneously
                                </p>
                            </div>

                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="p-3 bg-green-500/10 rounded-lg">
                                        <FaSun className="w-6 h-6 text-orange-400" />
                                    </div>
                                    <div>
                                        <h3 className="text-green-400 text-sm font-semibold">
                                            Solar Yield
                                        </h3>
                                        <p className="text-3xl text-white">
                                            {Math.round(selectedCluster.solarYield)} kWh/day
                                        </p>
                                    </div>
                                </div>
                                <p className="text-sm text-orange-400">
                                    {100} Solar panels installed
                                </p>
                            </div>

                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="p-3 bg-green-500/10 rounded-lg">
                                        <FaBolt className="w-6 h-6 text-blue-400" />
                                    </div>
                                    <div>
                                        <h3 className="text-green-400 text-sm font-semibold">
                                            Grid Reliance
                                        </h3>
                                        <p className="text-3xl text-white">
                                            {Math.round(selectedCluster.gridReliance)}%
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="p-3 bg-green-500/10 rounded-lg">
                                        <FaBatteryHalf className="w-6 h-6 text-green-400" />
                                    </div>
                                    <div>
                                        <h3 className="text-green-400 text-sm font-semibold">
                                            Battery Health
                                        </h3>
                                        <p className="text-3xl text-white">
                                            {Math.round(selectedCluster.batteryHealth)}%
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="p-3 bg-green-500/10 rounded-lg">
                                        <FaClock className="w-6 h-6 text-purple-400" />
                                    </div>
                                    <div>
                                        <h3 className="text-green-400 text-sm font-semibold">
                                            Uptime
                                        </h3>
                                        <p className="text-3xl text-white">
                                            {Math.round(selectedCluster.uptime)}%
                                        </p>
                                    </div>
                                </div>
                                <p className="text-sm text-purple-400">
                                    Last maintenance: 2 weeks ago
                                </p>
                            </div>

                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="p-3 bg-green-500/10 rounded-lg">
                                        <FaBell className="w-6 h-6 text-yellow-400" />
                                    </div>
                                    <div>
                                        <h3 className="text-green-400 text-sm font-semibold">
                                            Alarms
                                        </h3>
                                        <p className="text-2xl text-white">
                                            {selectedCluster.alarms.critical}{" "}
                                            Critical,{" "}
                                            {selectedCluster.alarms.warning}{" "}
                                            Warning,{" "}
                                            {selectedCluster.alarms.info} Info
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur">
                                <div className="flex items-center gap-4 mb-4">
                                    <div className="p-3 bg-green-500/10 rounded-lg">
                                        <FaTable className="w-6 h-6 text-green-400" />
                                    </div>
                                    <div>
                                        <h3 className="text-green-400 text-sm font-semibold">
                                            Number of Systems
                                        </h3>
                                        <p className="text-3xl text-white">
                                            {selectedCluster.numberOfSystems}
                                        </p>
                                    </div>
                                </div>
                                <p className="text-sm text-green-400">
                                    4 planned deployments in next quarter
                                </p>
                            </div>
                        </div>
                    </div>
                )}
            </Modal>
        </div>
    );
};

export default ClusterDashboard;
