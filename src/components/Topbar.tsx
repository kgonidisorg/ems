import React from "react";
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
    API: "/api",
    Compute: "/compute",
};

interface TopbarProps {
    selected?: MenuItemName;
}

const Topbar: React.FC<TopbarProps> = ({ selected }: TopbarProps) => {
    selected = selected || "Home";
    return (
        <div className="w-full bg-slate-900 shadow-lg">
            <Image
                src="/banner.jpg"
                alt="EcoGrid Banner"
                className="w-full h-32 object-cover object-bottom"
                width={1920}
                height={128}
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
                <ul className="flex space-x-6">
                    {Object.entries(menuItems).map(([name, href]) => (
                        <li key={name}>
                            <Link
                                href={href}
                                className={`hover:text-primary transition-colors ${
                                    selected === name
                                        ? "text-primary"
                                        : "text-secondary"
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
