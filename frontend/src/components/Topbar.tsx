/* eslint-disable @next/next/no-img-element */
"use client";
import React, { useState } from "react";
import Image from "next/image";
import Link from "next/link";

export type MenuItemName =
    | "Home"
    | "Network"
    | "EMS"
    | "Analytics"
    | "API"
    | "Compute";
const menuItems: Record<MenuItemName, string> = {
    Home: "/",
    Network: "/network",
    EMS: "/ems",
    Analytics: "/analytics",
    API: "/apidocs",
    Compute: "/compute",
};

interface TopbarProps {
    selected?: MenuItemName;
}

const Topbar: React.FC<TopbarProps> = ({ selected }: TopbarProps) => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    selected = selected || "Home";

    return (
        <div className="w-full bg-slate-900 shadow-lg">
            <img
                src="/banner.jpg"
                alt="EcoGrid Banner"
                className="w-full h-32 object-cover object-bottom"
            />
            <nav className="bg-slate-800 text-white py-3 shadow-md flex items-center justify-between px-6 text-2xl">
                <div className="flex items-center space-x-3">
                    <Image
                        src="/green-energy-icon.svg"
                        alt="EcoGrid Logo"
                        className="h-8 w-8"
                        width={32}
                        height={32}
                    />
                    <span className="font-bold text-primary">EcoGrid {selected}</span>
                </div>
                <button
                    className="md:hidden text-white hover:text-primary"
                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                >
                    <svg
                        className="w-6 h-6"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="2"
                            d="M4 6h16M4 12h16M4 18h16"
                        />
                    </svg>
                </button>
                <ul
                    className={`md:flex md:space-x-6 ${
                        isMenuOpen ? "block" : "hidden"
                    } md:block bg-slate-800 w-full md:w-auto top-0 md:top-auto left-0 md:relative absolute z-10`}
                    onClick={() => setIsMenuOpen(false)}
                >
                    {Object.entries(menuItems).map(([name, href]) => (
                        <li key={name} className="text-center md:text-left">
                            <Link
                                href={href}
                                className={`block py-2 px-4 hover:text-primary transition-colors ${
                                    selected === name ? "text-primary" : "text-secondary"
                                }`}
                            >
                                {name}
                            </Link>
                        </li>
                    ))}
                </ul>
            </nav>
        </div>
    );
};

export default Topbar;
