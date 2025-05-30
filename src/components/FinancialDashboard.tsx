import React from "react";
import {
    PieChart,
    Pie,
    Cell,
    Tooltip,
    Legend,
} from "recharts";

const data = [
    { name: "Energy Trading P&L", value: 400 },
    { name: "Demand-Charge Savings", value: 300 },
    { name: "ROI Projections", value: 500 },
];

const COLORS = ["#4caf50", "#2196f3", "#ff9800"];

const FinancialDashboard: React.FC = () => {
    return (
        <PieChart width={500} height={300}>
            <Pie
                data={data}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
            >
                {data.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
            </Pie>
            <Tooltip contentStyle={{ backgroundColor: "#1e293b", borderRadius: "8px", color: "#ffffff" }} />
            <Legend wrapperStyle={{ color: "#ffffff" }} />
        </PieChart>
    );
};

export default FinancialDashboard;
