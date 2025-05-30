import React, { useState } from "react";

const ReportScheduler: React.FC = () => {
    const [email, setEmail] = useState("");
    const [frequency, setFrequency] = useState("daily");

    const handleSchedule = () => {
        alert(`Report scheduled for ${email} with a ${frequency} frequency.`);
    };

    return (
        <div className="p-4">
            <h3 className="text-lg font-semibold text-green-400 mb-4">
                Schedule Reports
            </h3>
            <div className="mb-4">
                <label className="block text-white mb-2">Email Address</label>
                <input
                    type="email"
                    className="w-full p-2 rounded bg-slate-700 text-white border border-green-500"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
            </div>
            <div className="mb-4">
                <label className="block text-white mb-2">Frequency</label>
                <select
                    className="w-full p-2 rounded bg-slate-700 text-white border border-green-500"
                    value={frequency}
                    onChange={(e) => setFrequency(e.target.value)}
                >
                    <option value="daily">Daily</option>
                    <option value="weekly">Weekly</option>
                    <option value="monthly">Monthly</option>
                </select>
            </div>
            <button
                className="bg-green-500 text-white py-2 px-4 rounded hover:bg-green-600"
                onClick={handleSchedule}
            >
                Schedule Report
            </button>
        </div>
    );
};

export default ReportScheduler;
