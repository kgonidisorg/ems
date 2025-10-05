"use client";

import Topbar from "@/components/Topbar";
import TimeSeriesGraph from "@/components/TimeSeriesGraph";
import CO2Chart from "@/components/CO2Chart";
import FinancialDashboard from "@/components/FinancialDashboard";
import ReportScheduler from "@/components/ReportScheduler";

const AnalyticsPage: React.FC = () => {
    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-br from-slate-900 to-slate-800">
            <Topbar selected="Analytics" />
            <main className="flex-1 p-8 max-w-7xl mx-auto">
                {/* Header */}
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="text-5xl font-bold text-white mb-2">
                            Analytics & Reporting
                        </h1>
                        <p className="text-green-400">
                            Insights for Optimized Energy Management
                        </p>
                    </div>
                </div>

                {/* Time-Series Explorer */}
                <section className="mb-8 w-full">
                    <div className="p-6 rounded-lg bg-slate-800/50 border border-green-500/20 backdrop-blur">
                        <h2 className="text-2xl font-bold text-white mb-4">
                            Time-Series Explorer
                        </h2>
                        <p className="text-green-400 mb-4">
                            Multi-line graphs for solar, battery, EV, grid flows over any period
                        </p>
                        <div className="h-[400px] bg-slate-700 rounded-lg">
                            <TimeSeriesGraph hoursBack={24} />
                        </div>
                    </div>
                </section>

                {/* Sustainability & ROI */}
                <section className="mb-8">
                    <div className="p-6 rounded-lg bg-slate-800/50 border border-green-500/20 backdrop-blur">
                        <h2 className="text-2xl font-bold text-white mb-4">
                            Sustainability & ROI
                        </h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="bg-slate-700 p-4 rounded-lg">
                                <h3 className="text-lg font-semibold text-green-400 mb-2">
                                    CO₂ Avoided Chart
                                </h3>
                                <div className="h-[300px] bg-slate-800 rounded-lg">
                                    <CO2Chart />
                                </div>
                            </div>
                            <div className="bg-slate-700 p-4 rounded-lg">
                                <h3 className="text-lg font-semibold text-green-400 mb-2">
                                    Financial Dashboard
                                </h3>
                                <div className="h-[300px] bg-slate-800 rounded-lg">
                                    <FinancialDashboard />
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                {/* Automated Reports */}
                <section className="mb-8">
                    <div className="p-6 rounded-lg bg-slate-800/50 border border-green-500/20 backdrop-blur">
                        <h2 className="text-2xl font-bold text-white mb-4">
                            Automated Reports
                        </h2>
                        <p className="text-green-400 mb-4">
                            Scheduled PDF/CSV exports for investors or regulators
                        </p>
                        <div className="h-fit bg-slate-700 rounded-lg">
                            <ReportScheduler />
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
};

export default AnalyticsPage;