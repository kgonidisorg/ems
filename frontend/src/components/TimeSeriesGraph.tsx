"use client";
import React, { useRef, useState, useEffect } from "react";
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    Tooltip,
    Legend,
} from "recharts";
import { analyticsAPI } from "@/lib/analytics";
import { useAsyncData } from "@/hooks/useAsyncData";
import { LoadingSpinner, ErrorDisplay } from "@/components/ui/LoadingComponents";

export interface TimeSeriesGraphProps {
  siteId?: number;
  hoursBack?: number;
}

const TimeSeriesGraph: React.FC<TimeSeriesGraphProps> = ({ siteId, hoursBack = 24 }) => {
    const containerRef = useRef<HTMLDivElement>(null);
    const [dimensions, setDimensions] = useState({ width: 0, height: 0 });

    // Fetch energy consumption data specifically for time series (avoid duplicate dashboard calls)
    const { data: energyData, loading, error, refetch } = useAsyncData(
        () => analyticsAPI.getEnergyConsumption({ 
            aggregation: 'HOURLY',
            siteId,
            startDate: new Date(Date.now() - (hoursBack || 24) * 60 * 60 * 1000).toISOString(),
            endDate: new Date().toISOString()
        }),
        { dependencies: [hoursBack, siteId] }
    );

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

    // Transform backend data to chart format
    const chartData = Array.isArray(energyData?.dataPoints) ? energyData!.dataPoints.map(point => ({
        name: new Date(point.timestamp).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }),
        consumption: point.consumption ?? 0,
        // Deterministic generated value for consistent SSR/CSR
        generated: (point.consumption ?? 0) * 1.15,
        carbonSaved: (point.consumption ?? 0) * 0.5,
        costSavings: (point.consumption ?? 0) * 0.1
    })) : [];

    if (loading) {
        return <LoadingSpinner size="md" message="Loading time series data..." />;
    }

    if (error) {
        return <ErrorDisplay error={error} onRetry={refetch} />;
    }

    return (
        <div ref={containerRef} style={{ width: "100%", height: "100%" }}>
            <LineChart
                width={dimensions.width}
                height={dimensions.height}
                data={chartData}
                margin={{ top: 40, right: 40, left: 20, bottom: 20 }}
            >
                <XAxis dataKey="name" stroke="#ffffff" />
                <YAxis stroke="#ffffff" />
                <Tooltip contentStyle={{ backgroundColor: "#1e293b", borderRadius: "8px", color: "#ffffff" }} />
                <Legend wrapperStyle={{ color: "#ffffff" }} />
                <Line type="monotone" dataKey="generated" stroke="#4caf50" strokeWidth={2} name="Energy Generated (kWh)" />
                <Line type="monotone" dataKey="consumption" stroke="#2196f3" strokeWidth={2} name="Energy Consumed (kWh)" />
                <Line type="monotone" dataKey="carbonSaved" stroke="#ff9800" strokeWidth={2} name="Carbon Saved (kg)" />
                <Line type="monotone" dataKey="costSavings" stroke="#f44336" strokeWidth={2} name="Cost Savings ($)" />
            </LineChart>
        </div>
    );
};

export default TimeSeriesGraph;
