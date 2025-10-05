"use client";
import React from "react";
import {
    PieChart,
    Pie,
    Cell,
    Tooltip,
    Legend,
} from "recharts";
import { analyticsAPI } from "@/lib/analytics";
import { useAsyncData } from "@/hooks/useAsyncData";
import { LoadingSpinner, ErrorDisplay } from "@/components/ui/LoadingComponents";

const COLORS = ["#4caf50", "#2196f3", "#ff9800"];

export interface FinancialDashboardProps {
  siteId?: number;
  startDate?: string;
  endDate?: string;
}

const FinancialDashboard: React.FC<FinancialDashboardProps> = ({ siteId, startDate, endDate }) => {
    // Fetch financial metrics data
    const { data: financialData, loading, error, refetch } = useAsyncData(
        () => analyticsAPI.getFinancialMetrics({ siteId, startDate, endDate }),
        { dependencies: [siteId, startDate, endDate] }
    );

    // Transform data for pie chart (defensive)
    type ChartEntry = { name: string; value: number };
    const chartData: ChartEntry[] = financialData ? [
        { name: "Revenue", value: financialData.totalRevenue ?? 0 },
        { name: "Cost Savings", value: financialData.totalCosts ?? 0 },
        { name: "Net Profit", value: financialData.netProfit ?? 0 },
    ] : [];

    if (loading) {
        return <LoadingSpinner size="sm" message="Loading financial data..." />;
    }

    if (error) {
        return <ErrorDisplay error={error} onRetry={refetch} />;
    }

    return (
        <PieChart width={500} height={300}>
            <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
            >
                {chartData.map((entry: ChartEntry, index: number) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
            </Pie>
            <Tooltip contentStyle={{ backgroundColor: "#1e293b", borderRadius: "8px", color: "#ffffff" }} />
            <Legend wrapperStyle={{ color: "#ffffff" }} />
        </PieChart>
    );
};

export default FinancialDashboard;
