import React, { useEffect, useRef, useState } from "react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip } from "recharts";

const data = [
    { name: "Jan", co2: 400 },
    { name: "Feb", co2: 300 },
    { name: "Mar", co2: 500 },
    { name: "Apr", co2: 700 },
    { name: "May", co2: 600 },
    { name: "Jun", co2: 800 },
];

const CO2Chart: React.FC = () => {
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
        <div ref={containerRef} className="w-full h-full">
            <BarChart
                width={dimensions.width}
                height={dimensions.height}
                data={data}
                margin={{ top: 40, right: 40, left: 0, bottom: 20 }}
            >
                <XAxis dataKey="name" stroke="#ffffff" />
                <YAxis stroke="#ffffff" />
                <Tooltip
                    contentStyle={{
                        backgroundColor: "#1e293b",
                        borderRadius: "8px",
                        color: "#ffffff",
                    }}
                />
                <Bar dataKey="co2" fill="#4caf50" />
            </BarChart>
        </div>
    );
};

export default CO2Chart;
