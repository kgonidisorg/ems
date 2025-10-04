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
import dynamic from "next/dynamic";
import Modal from "react-modal";
import Topbar from "@/components/Topbar";

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
    const [clusters] = useState<Cluster[]>([
        {
            name: "California",
            lat: 36.7783,
            lng: -119.4179,
            avgSOC: 85,
            evThroughput: 120,
            solarYield: 500,
            gridReliance: 30,
            revenueBreakdown: {
                ancillary: 200,
                demandResponse: 150,
                arbitrage: 100,
            },
            carbonOffset: 250,
            batteryHealth: 90,
            uptime: 99.5,
            alarms: { critical: 1, warning: 2, info: 5 },
            numberOfSystems: 50,
        },
        {
            name: "New York",
            lat: 43.2994, // Updated latitude
            lng: -74.2179, // Updated longitude
            avgSOC: 78,
            evThroughput: 95,
            solarYield: 400,
            gridReliance: 40,
            revenueBreakdown: {
                ancillary: 180,
                demandResponse: 120,
                arbitrage: 80,
            },
            carbonOffset: 200,
            batteryHealth: 85,
            uptime: 98.7,
            alarms: { critical: 0, warning: 1, info: 3 },
            numberOfSystems: 40, // Added number of systems
        },
        {
            name: "Michigan",
            lat: 44.1822, // Updated latitude
            lng: -84.5068, // Updated longitude
            avgSOC: 82,
            evThroughput: 110,
            solarYield: 450,
            gridReliance: 35,
            revenueBreakdown: {
                ancillary: 190,
                demandResponse: 140,
                arbitrage: 90,
            },
            carbonOffset: 220,
            batteryHealth: 88,
            uptime: 99.0,
            alarms: { critical: 1, warning: 1, info: 4 },
            numberOfSystems: 35, // Added number of systems
        },
        {
            name: "Florida",
            lat: 27.9944, // Correct latitude
            lng: -81.7603, // Correct longitude
            avgSOC: 80,
            evThroughput: 100,
            solarYield: 420,
            gridReliance: 38,
            revenueBreakdown: {
                ancillary: 170,
                demandResponse: 130,
                arbitrage: 85,
            },
            carbonOffset: 210,
            batteryHealth: 87,
            uptime: 98.5,
            alarms: { critical: 0, warning: 2, info: 3 },
            numberOfSystems: 45, // Added number of systems
        },
    ]);

    const [selectedCluster, setSelectedCluster] = useState<Cluster | null>(
        null
    );
    const [isModalOpen, setIsModalOpen] = useState(false);

    const openModal = (cluster: Cluster) => {
        setSelectedCluster(cluster);
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setSelectedCluster(null);
        setIsModalOpen(false);
    };

    useEffect(() => {
        Modal.setAppElement("#__next");
    }, []);

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
                                <ClusterMapView
                                    sites={clusters.map((cluster) => ({
                                        lat: cluster.lat,
                                        lng: cluster.lng,
                                        name: cluster.name,
                                        soc: cluster.avgSOC,
                                        onClick: () => openModal(cluster),
                                    }))}
                                />
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

                        {/* Cluster Table */}
                        <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur mb-8">
                            <h2 className="text-2xl font-bold text-white mb-6">
                                Cluster Table
                            </h2>
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
                                    {clusters.map((cluster) => (
                                        <tr
                                            key={cluster.name}
                                            className="border-t border-slate-700"
                                        >
                                            <td className="p-4">
                                                {cluster.name}
                                            </td>
                                            <td className="p-4">
                                                {cluster.avgSOC}%
                                            </td>
                                            <td className="p-4">
                                                {cluster.evThroughput} kWh/hr
                                            </td>
                                            <td className="p-4">
                                                {cluster.solarYield} kWh/day
                                            </td>
                                            <td className="p-4">
                                                {cluster.gridReliance}%
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
                                            {selectedCluster.avgSOC}%
                                        </p>
                                    </div>
                                </div>
                                <div className="h-1 w-full bg-slate-700 rounded">
                                    <div
                                        className="h-1 bg-green-400 rounded"
                                        style={{
                                            width: `${selectedCluster.avgSOC}%`,
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
                                            {selectedCluster.evThroughput}{" "}
                                            kWh/hr
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
                                            {selectedCluster.solarYield} kWh/day
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
                                            {selectedCluster.gridReliance}%
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
                                            {selectedCluster.batteryHealth}%
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
                                            {selectedCluster.uptime}%
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
