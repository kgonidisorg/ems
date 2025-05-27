"use client";
import React, { useState } from "react";
import {
    FaHome,
    FaMap,
    FaNetworkWired,
    FaLeaf,
    FaChartPie,
    FaChevronLeft,
    FaChevronRight,
} from "react-icons/fa";

type labelType =
    | "Overview"
    | "Nationwide Map"
    | "Network Status"
    | "Renewable Mix"
    | "Sustainability Impact";

const menuItems: { href: string; label: labelType; icon: React.JSX.Element }[] =
    [
        { href: "/", label: "Overview", icon: <FaHome /> },
        { href: "#map", label: "Nationwide Map", icon: <FaMap /> },
        { href: "#status", label: "Network Status", icon: <FaNetworkWired /> },
        { href: "#renewables", label: "Renewable Mix", icon: <FaLeaf /> },
        {
            href: "#sustainability",
            label: "Sustainability Impact",
            icon: <FaChartPie />,
        },
    ];

const Sidebar = ({ selected }: { selected: labelType }) => {
    const [isCollapsed, setIsCollapsed] = useState(false);

    return (
        <aside
            className={`${
                isCollapsed ? "w-20" : "w-64"
            } bg-slate-800 text-white h-screen fixed top-0 left-0 shadow-lg transition-width duration-300`}
        >
            <div className="p-6 border-b border-slate-700 flex items-center justify-between">
                <h1
                    className={`text-2xl font-bold text-green-400 ${
                        isCollapsed ? "hidden" : ""
                    }`}
                >
                    EcoGrid
                </h1>
                <button
                    onClick={() => setIsCollapsed(!isCollapsed)}
                    className="text-green-400 focus:outline-none"
                >
                    {isCollapsed ? <FaChevronRight /> : <FaChevronLeft />}
                </button>
            </div>
            <nav className="p-6 space-y-4">
                {menuItems.map((item) => (
                    <a
                        key={item.href}
                        href={item.href}
                        className={`flex items-center gap-4 hover:text-green-400 ${
                            selected === item.label
                                ? "text-green-500"
                                : "text-slate-300"
                        }`}
                    >
                        <span className="text-xl">{item.icon}</span>
                        {!isCollapsed && <span>{item.label}</span>}
                    </a>
                ))}
            </nav>
        </aside>
    );
};

export default Sidebar;
