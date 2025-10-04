import React from "react";
import Topbar from "@/components/Topbar";

const ComputePage: React.FC = () => {
    return (
        <div className="flex min-h-screen flex-col bg-gradient-to-br from-slate-900 to-slate-800">
            <Topbar selected="Compute" />
            <main className="flex-1 flex flex-col items-center justify-center">
                <h1 className="text-4xl font-bold text-white mb-4">Sorry, this page is not available in the demo</h1>
                <p className="text-green-400 text-lg">Please contact support for more information.</p>
            </main>
        </div>
    );
};

export default ComputePage;