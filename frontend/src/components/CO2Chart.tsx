"use client";
import React, { useEffect, useRef, useState } from "react";
import { BarChart, Bar, XAxis, YAxis, Tooltip } from "recharts";
import { AnalyticsService } from "@/lib/api";
import { useAsyncData } from "@/hooks/useAsyncData";
import { LoadingSpinner, ErrorDisplay } from "@/components/ui/LoadingComponents";

export interface CO2ChartProps {
  siteId?: number;
  startDate?: string;
  endDate?: string;
}

const CO2Chart: React.FC<CO2ChartProps> = ({ siteId, startDate, endDate }) => {
    const containerRef = useRef<HTMLDivElement>(null);
    const [dimensions, setDimensions] = useState({ width: 0, height: 0 });

    // Fetch carbon footprint data
    const { data: carbonData, loading, error, refetch } = useAsyncData(
        () => AnalyticsService.getCarbonFootprint({ siteId, startDate, endDate }),
        { dependencies: [siteId, startDate, endDate] }
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

    // Transform data for chart (defensive)
    const chartData = Array.isArray(carbonData?.dataPoints) ? carbonData!.dataPoints.map(point => ({
        name: new Date(point.timestamp).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
        co2: point.carbon ?? 0
    })) : [];

    if (loading) {
        return <LoadingSpinner size="sm" message="Loading carbon data..." />;
    }

    if (error) {
        return <ErrorDisplay error={error} onRetry={refetch} />;
    }

    return (
        <div ref={containerRef} className="w-full h-full">
            <BarChart
                width={dimensions.width}
                height={dimensions.height}
                data={chartData}
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
