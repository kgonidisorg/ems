/* eslint-disable @next/next/no-img-element */
"use client";
import React, { useRef, useState, useEffect } from "react";

/**
 * EnergyFlowDiagram Component
 * Isometric layout with animated energy flows between battery, solar, and facility.
 */
export default function EnergyFlowDiagram() {
    const containerRef = useRef<HTMLDivElement>(null);
    const [size, setSize] = useState({ width: 0, height: 0 });

    useEffect(() => {
        function onResize() {
            if (containerRef.current) {
                setSize({
                    width: containerRef.current.clientWidth,
                    height: containerRef.current.clientHeight,
                });
            }
        }
        onResize();
        window.addEventListener("resize", onResize);
        return () => window.removeEventListener("resize", onResize);
    }, []);

    // Percentage positions for isometric angles
    const positions = {
        battery: { x: 50, y: 50 }, // left-center
        solar: { x: 80, y: 20 }, // top-right
        facility: { x: 20, y: 80 }, // bottom-right
    };

    return (
        <div
            ref={containerRef}
            className="relative w-full h-80 sm:h-96 bg-transparent"
            style={{ perspective: "800px" }}
        >
            {/* Battery */}
            <div
                className="absolute z-1 -translate-[50%] lg:-translate-[60%]"
                style={{
                    left: `${positions.battery.x}%`,
                    top: `${positions.battery.y}%`,
                }}
            >
                <img
                    src="/bms.webp"
                    alt="Battery"
                    className="w-32 lg:w-64"
                />
            </div>

            {/* Solar Array */}
            <div
                className="absolute z-1 -translate-[60%] lg:-translate-[65%]"
                style={{
                    left: `${positions.solar.x}%`,
                    top: `${positions.solar.y}%`,
                }}
            >
                <img
                    src="solar.webp"
                    alt="Solar Array"
                    className="w-32 lg:w-84"
                />
            </div>

            {/* Facility */}
            <div
                className="absolute z-1 -translate-[50%] lg:-translate-[60%] rotate-6"
                style={{
                    left: `${positions.facility.x}%`,
                    top: `${positions.facility.y}%`,
                    // transform: "translate(-60%, -60%) scale(1.5) rotate(6deg)",
                }}
            >
                <img
                    src="facility.webp"
                    alt="Facility"
                    className="w-32 lg:w-64"
                />
            </div>

            {/* Animated SVG Flows */}
            <svg
                viewBox={`0 0 ${size.width} ${size.height}`}
                className="absolute inset-0 z-0"
            >
                <defs>
                    <linearGradient
                        id="unique-water-gradient"
                        gradientUnits="userSpaceOnUse"
                        x1="0%"
                        y1="0%"
                        x2="100%"
                        y2="0%"
                    >
                        <stop offset="20%" stopColor="#4caf50" stopOpacity="1">
                            <animate
                                attributeName="offset"
                                values="0;1"
                                dur="1s"
                                repeatCount="indefinite"
                            />
                        </stop>
                        <stop offset="50%" stopColor="#2196f3" stopOpacity="1">
                            <animate
                                attributeName="offset"
                                values="0.5;1.5"
                                dur="1s"
                                repeatCount="indefinite"
                            />
                        </stop>
                        <stop offset="100%" stopColor="#0f0" stopOpacity="1">
                            <animate
                                attributeName="offset"
                                values="1;2"
                                dur="1s"
                                repeatCount="indefinite"
                            />
                        </stop>
                    </linearGradient>
                </defs>
                {["solar", "facility"].map((target) => {
                    const start = positions.battery;
                    const end = positions[target as "solar" | "facility"];
                    const x1 = (start.x / 100) * size.width;
                    const y1 = (start.y / 100) * size.height;
                    const x2 = (end.x / 100) * size.width;
                    const y2 = (end.y / 100) * size.height;
                    return (
                        <path
                            key={target}
                            d={`M${x1},${y1} L${x2},${y2}`}
                            className="flow-path"
                        />
                    );
                })}
            </svg>
            {/* Styling */}
            <style jsx>{`
                .flow-path {
                    stroke: url(#unique-water-gradient);
                    stroke-width: 10;
                    fill: none; /* Ensure no fill is applied */
                    filter: drop-shadow(0 0 10px rgba(33, 150, 243, 0.8)); /* Add glow effect */
                }
            `}</style>
        </div>
    );
}
