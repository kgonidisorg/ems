/* eslint-disable @next/next/no-img-element */
"use client";
import React, { useState, useEffect, useRef } from "react";
import Image from "next/image";
import Link from "next/link";
import { AuthService } from "../lib/auth";
import { useRouter } from "next/navigation";

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

interface UserDropdownProps {
    onLogout: () => void;
    isLoggingOut: boolean;
}

const UserDropdown: React.FC<UserDropdownProps> = ({ onLogout, isLoggingOut }) => {
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsDropdownOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const handleLogout = () => {
        setIsDropdownOpen(false);
        onLogout();
    };

    return (
        <div className="relative" ref={dropdownRef}>
            <button
                onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                className="flex items-center space-x-2 text-secondary hover:text-primary transition-colors p-2 rounded-lg hover:bg-slate-700"
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
                        d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                    />
                </svg>
            </button>
            
            {isDropdownOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-slate-800 rounded-lg shadow-lg border border-slate-700 z-20 md:z-20">
                    <div className="py-1">
                        <button
                            onClick={handleLogout}
                            disabled={isLoggingOut}
                            className="w-full text-left px-4 py-2 text-white hover:text-primary hover:bg-slate-700 transition-colors flex items-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {isLoggingOut ? (
                                <svg
                                    className="w-4 h-4 animate-spin"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                >
                                    <circle
                                        className="opacity-25"
                                        cx="12"
                                        cy="12"
                                        r="10"
                                        stroke="currentColor"
                                        strokeWidth="4"
                                    />
                                    <path
                                        className="opacity-75"
                                        fill="currentColor"
                                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                                    />
                                </svg>
                            ) : (
                                <svg
                                    className="w-4 h-4"
                                    fill="none"
                                    stroke="currentColor"
                                    viewBox="0 0 24 24"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth="2"
                                        d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                                    />
                                </svg>
                            )}
                            <span>{isLoggingOut ? 'Logging out...' : 'Logout'}</span>
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

const Topbar: React.FC<TopbarProps> = ({ selected }: TopbarProps) => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isLoggingOut, setIsLoggingOut] = useState(false);
    const router = useRouter();
    selected = selected || "Home";

    const handleLogout = async () => {
        try {
            setIsLoggingOut(true);
            setIsMenuOpen(false);
            // Call the AuthService logout method
            await AuthService.logout();
            // Redirect to login page after logout
            router.push('/login');
        } catch (error) {
            console.error('Logout failed:', error);
            // Even if logout fails, try to redirect to login
            try {
                router.push('/login');
            } catch (navError) {
                console.error('Navigation after logout failed:', navError);
                // Fallback: reload to login page
                window.location.href = '/login';
            }
        } finally {
            setIsLoggingOut(false);
        }
    };

    return (
        <div className="w-full bg-slate-900 shadow-lg">
            <img
                src="/banner.jpg"
                alt="EcoGrid Banner"
                className="w-full h-32 object-cover object-bottom"
            />
            <nav className="bg-slate-800 text-white py-3 shadow-md flex items-center justify-between px-6 text-2xl relative">
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
                
                {/* Desktop Menu */}
                <ul className="hidden md:flex md:space-x-6">
                    {Object.entries(menuItems).map(([name, href]) => (
                        <li key={name}>
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

                {/* Right side controls */}
                <div className="flex items-center space-x-4">
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
                    <div className="hidden md:block">
                        <UserDropdown onLogout={handleLogout} isLoggingOut={isLoggingOut} />
                    </div>
                </div>

                {/* Mobile Menu */}
                <ul
                    className={`md:hidden ${
                        isMenuOpen ? "block" : "hidden"
                    } absolute top-full left-0 w-full bg-slate-800 border-t border-slate-700 z-10`}
                >
                    {Object.entries(menuItems).map(([name, href]) => (
                        <li key={name} className="border-b border-slate-700">
                            <Link
                                href={href}
                                className={`block py-3 px-6 hover:text-primary transition-colors ${
                                    selected === name ? "text-primary" : "text-secondary"
                                }`}
                                onClick={() => setIsMenuOpen(false)}
                            >
                                {name}
                            </Link>
                        </li>
                    ))}
                    <li className="border-b border-slate-700 last:border-b-0">
                        <button
                            onClick={handleLogout}
                            disabled={isLoggingOut}
                            className="w-full text-left py-3 px-6 text-secondary hover:text-primary transition-colors flex items-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {isLoggingOut ? (
                                <svg
                                    className="w-4 h-4 animate-spin"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                >
                                    <circle
                                        className="opacity-25"
                                        cx="12"
                                        cy="12"
                                        r="10"
                                        stroke="currentColor"
                                        strokeWidth="4"
                                    />
                                    <path
                                        className="opacity-75"
                                        fill="currentColor"
                                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                                    />
                                </svg>
                            ) : (
                                <svg
                                    className="w-4 h-4"
                                    fill="none"
                                    stroke="currentColor"
                                    viewBox="0 0 24 24"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth="2"
                                        d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                                    />
                                </svg>
                            )}
                            <span>{isLoggingOut ? 'Logging out...' : 'Logout'}</span>
                        </button>
                    </li>
                </ul>
            </nav>
        </div>
    );
};

export default Topbar;
