import React from 'react';

const Filters: React.FC<{
  regions: string[];
  groups: string[];
  onRegionChange: (region: string) => void;
  onGroupChange: (group: string) => void;
}> = ({ regions, groups, onRegionChange, onGroupChange }) => {
  return (
    <div className="flex space-x-4 mb-4">
      <select onChange={(e) => onRegionChange(e.target.value)} className="select-box">
        <option value="">Select Region</option>
        {regions.map((region) => (
          <option key={region} value={region}>
            {region}
          </option>
        ))}
      </select>
      <select onChange={(e) => onGroupChange(e.target.value)} className="select-box">
        <option value="">Select Group</option>
        {groups.map((group) => (
          <option key={group} value={group}>
            {group}
          </option>
        ))}
      </select>
    </div>
  );
};

export default Filters;