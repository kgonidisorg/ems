import React, { useState, useRef, useEffect } from 'react';
import { SiteOption } from '@/lib/types';
import { FaChevronDown, FaSearch, FaSpinner } from 'react-icons/fa';

interface SiteSelectorProps {
  sites: SiteOption[];
  selectedSiteId: number | null;
  onSiteChange: (siteId: number | null) => void;
  loading?: boolean;
  error?: string | null;
  placeholder?: string;
  className?: string;
}

/**
 * Searchable site selector dropdown component
 */
export const SiteSelector: React.FC<SiteSelectorProps> = ({
  sites,
  selectedSiteId,
  onSiteChange,
  loading = false,
  error = null,
  placeholder = 'Select a site...',
  className = '',
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const dropdownRef = useRef<HTMLDivElement>(null);
  const searchInputRef = useRef<HTMLInputElement>(null);

  // Find selected site
  const selectedSite = sites.find(site => site.id === selectedSiteId);

  // Filter sites based on search term
  const filteredSites = sites.filter(site =>
    site.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Handle click outside to close dropdown
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
        setSearchTerm('');
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Focus search input when dropdown opens
  useEffect(() => {
    if (isOpen && searchInputRef.current) {
      searchInputRef.current.focus();
    }
  }, [isOpen]);

  const handleToggleDropdown = () => {
    if (!loading && !error) {
      setIsOpen(!isOpen);
      setSearchTerm('');
    }
  };

  const handleSiteSelect = (site: SiteOption) => {
    onSiteChange(site.id);
    setIsOpen(false);
    setSearchTerm('');
  };

  const handleClearSelection = () => {
    onSiteChange(null);
    setIsOpen(false);
    setSearchTerm('');
  };

  return (
    <div className={`relative ${className}`} ref={dropdownRef}>
      {/* Main Selector Button */}
      <button
        type="button"
        onClick={handleToggleDropdown}
        disabled={loading || !!error}
        className={`
          w-full px-4 py-3 text-left bg-slate-800 border border-green-500/50 rounded-lg
          text-white hover:border-green-500 focus:border-green-500 focus:outline-none
          focus:ring-2 focus:ring-green-500/20 transition-colors duration-200
          disabled:opacity-50 disabled:cursor-not-allowed
          flex items-center justify-between
        `}
      >
        <span className="flex items-center gap-3">
          {loading && <FaSpinner className="animate-spin text-green-400" />}
          <span className="truncate">
            {error ? (
              <span className="text-red-400">Error loading sites</span>
            ) : selectedSite ? (
              selectedSite.name
            ) : (
              <span className="text-slate-400">{placeholder}</span>
            )}
          </span>
        </span>
        {!loading && !error && (
          <FaChevronDown 
            className={`text-green-400 transition-transform duration-200 ${
              isOpen ? 'rotate-180' : ''
            }`} 
          />
        )}
      </button>

      {/* Dropdown */}
      {isOpen && !loading && !error && (
        <div className="absolute z-50 w-full mt-2 bg-slate-800 border border-green-500/50 rounded-lg shadow-xl">
          {/* Search Input */}
          <div className="p-3 border-b border-slate-700">
            <div className="relative">
              <FaSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400" />
              <input
                ref={searchInputRef}
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search sites..."
                className="
                  w-full pl-10 pr-4 py-2 bg-slate-700 border border-slate-600 rounded-md
                  text-white placeholder-slate-400 focus:border-green-500 focus:outline-none
                  focus:ring-2 focus:ring-green-500/20
                "
              />
            </div>
          </div>

          {/* Options List */}
          <div className="max-h-60 overflow-y-auto">
            {/* Clear Selection Option */}
            {selectedSiteId && (
              <>
                <button
                  type="button"
                  onClick={handleClearSelection}
                  className="
                    w-full px-4 py-3 text-left hover:bg-slate-700 transition-colors
                    text-slate-400 italic border-b border-slate-700
                  "
                >
                  Clear selection
                </button>
              </>
            )}

            {/* Site Options */}
            {filteredSites.length > 0 ? (
              filteredSites.map((site) => (
                <button
                  key={site.id}
                  type="button"
                  onClick={() => handleSiteSelect(site)}
                  className={`
                    w-full px-4 py-3 text-left hover:bg-slate-700 transition-colors
                    ${site.id === selectedSiteId 
                      ? 'bg-green-500/10 text-green-400 border-r-2 border-green-500' 
                      : 'text-white'
                    }
                  `}
                >
                  <div className="truncate">{site.name}</div>
                  <div className="text-xs text-slate-400 mt-1">ID: {site.id}</div>
                </button>
              ))
            ) : (
              <div className="px-4 py-6 text-center text-slate-400">
                {searchTerm ? `No sites found matching "${searchTerm}"` : 'No sites available'}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="mt-2 text-sm text-red-400">
          {error}
        </div>
      )}
    </div>
  );
};