import React from 'react';

const KPIStrip: React.FC<{
  capacity: string;
  carbonOffset: string;
  revenue: string;
  renewablesPercentage: string;
}> = ({ capacity, carbonOffset, revenue, renewablesPercentage }) => {
  return (
    <div className="grid grid-cols-4 gap-4 bg-muted p-4 rounded-lg shadow-md">
      <div>
        <h3 className="text-lg font-bold">Installed Capacity</h3>
        <p>{capacity} MWh</p>
      </div>
      <div>
        <h3 className="text-lg font-bold">Carbon Offset</h3>
        <p>{carbonOffset} tons CO₂</p>
      </div>
      <div>
        <h3 className="text-lg font-bold">Revenue</h3>
        <p>${revenue}</p>
      </div>
      <div>
        <h3 className="text-lg font-bold">Renewables %</h3>
        <p>{renewablesPercentage}%</p>
      </div>
    </div>
  );
};

export default KPIStrip;