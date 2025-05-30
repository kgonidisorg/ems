import Topbar from "@/components/Topbar";

const APIDocsPage: React.FC = () => {
    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-br from-slate-900 to-slate-800">
            <Topbar selected="API" />
            <main className="flex-1 p-8 w-7xl mx-auto">
                {/* Header */}
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="text-5xl font-bold text-white mb-2">
                            API Documentation
                        </h1>
                        <p className="text-green-400">
                            Interactive and Responsive API Reference
                        </p>
                    </div>
                </div>

                {/* API Endpoints */}
                <section className="mb-8">
                    <div className="p-6 rounded-lg bg-slate-800/50 border border-green-500/20 backdrop-blur">
                        <h2 className="text-2xl font-bold text-white mb-4">
                            Endpoints
                        </h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div className="bg-slate-700 p-4 rounded-lg">
                                <h3 className="text-lg font-semibold text-green-400 mb-2">
                                    GET /analytics
                                </h3>
                                <p className="text-white mb-4">
                                    Fetch analytics data including time-series graphs, CO₂ avoided, and financial metrics.
                                </p>
                                <button className="bg-green-500 text-white py-2 px-4 rounded hover:bg-green-600">
                                    Try It Out
                                </button>
                            </div>
                            <div className="bg-slate-700 p-4 rounded-lg">
                                <h3 className="text-lg font-semibold text-green-400 mb-2">
                                    POST /reports/schedule
                                </h3>
                                <p className="text-white mb-4">
                                    Schedule automated reports by providing email and frequency.
                                </p>
                                <button className="bg-green-500 text-white py-2 px-4 rounded hover:bg-green-600">
                                    Try It Out
                                </button>
                            </div>
                        </div>
                    </div>
                </section>

                {/* Interactive API Tester */}
                <section className="mb-8">
                    <div className="p-6 rounded-lg bg-slate-800/50 border border-green-500/20 backdrop-blur">
                        <h2 className="text-2xl font-bold text-white mb-4">
                            Interactive API Tester
                        </h2>
                        <div className="bg-slate-700 p-4 rounded-lg">
                            <p className="text-white mb-4">
                                Use the form below to test API endpoints directly.
                            </p>
                            <form className="grid grid-cols-1 gap-4">
                                <div>
                                    <label className="block text-white mb-2">Endpoint</label>
                                    <input
                                        type="text"
                                        className="w-full p-2 rounded bg-slate-700 text-white border border-green-500"
                                        placeholder="e.g., /analytics"
                                    />
                                </div>
                                <div>
                                    <label className="block text-white mb-2">Request Body</label>
                                    <textarea
                                        className="w-full p-2 rounded bg-slate-700 text-white border border-green-500"
                                        rows={4}
                                        placeholder='e.g., { "email": "example@example.com", "frequency": "daily" }'
                                    ></textarea>
                                </div>
                                <button
                                    type="submit"
                                    className="bg-green-500 text-white py-2 px-4 rounded hover:bg-green-600"
                                >
                                    Send Request
                                </button>
                            </form>
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
};

export default APIDocsPage;