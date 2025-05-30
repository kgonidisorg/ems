import React, { useRef, useState, useEffect } from "react";
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    Tooltip,
    Legend,
} from "recharts";

const data = [
    { name: "00:00", solar: 400, battery: 240, ev: 240, grid: 400 },
    { name: "06:00", solar: 300, battery: 139, ev: 221, grid: 300 },
    { name: "12:00", solar: 200, battery: 980, ev: 229, grid: 200 },
    { name: "18:00", solar: 278, battery: 390, ev: 200, grid: 278 },
    { name: "24:00", solar: 189, battery: 480, ev: 218, grid: 189 },
];

const TimeSeriesGraph: React.FC = () => {
    const containerRef = useRef<HTMLDivElement>(null);
    const [dimensions, setDimensions] = useState({ width: 0, height: 0 });

    useEffect(() => {
        const updateDimensions = () => {
            if (containerRef.current) {
                setDimensions({
                    width: containerRef.current.offsetWidth,
                    height: containerRef.current.offsetHeight,
                });
            }
        };

        updateDimensions();
        window.addEventListener("resize", updateDimensions);

        return () => {
            window.removeEventListener("resize", updateDimensions);
        };
    }, []);

    return (
        <div ref={containerRef} style={{ width: "100%", height: "100%" }}>
            <LineChart
                width={dimensions.width}
                height={dimensions.height}
                data={data}
                margin={{ top: 40, right: 40, left: 20, bottom: 20 }}
            >
                <XAxis dataKey="name" stroke="#ffffff" />
                <YAxis stroke="#ffffff" />
                <Tooltip contentStyle={{ backgroundColor: "#1e293b", borderRadius: "8px", color: "#ffffff" }} />
                <Legend wrapperStyle={{ color: "#ffffff" }} />
                <Line type="monotone" dataKey="solar" stroke="#4caf50" strokeWidth={2} />
                <Line type="monotone" dataKey="battery" stroke="#2196f3" strokeWidth={2} />
                <Line type="monotone" dataKey="ev" stroke="#ff9800" strokeWidth={2} />
                <Line type="monotone" dataKey="grid" stroke="#f44336" strokeWidth={2} />
            </LineChart>
        </div>
    );
};

export default TimeSeriesGraph;
