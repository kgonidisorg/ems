import React, { useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import mapboxgl from 'mapbox-gl';
import type { Site } from '@/lib/types';

mapboxgl.accessToken = 'pk.eyJ1Ijoia2dvbmlkaXMiLCJhIjoiY2tlY3hyZHBkMGdidjJxcDNyanhqZmF0ZCJ9.fpUmRv0Mb7XwB7cYojQGiA';

interface MapViewProps {
  sites: Site[];
}

const MapView: React.FC<MapViewProps> = ({ sites }) => {
  const mapContainerRef = useRef<HTMLDivElement | null>(null);
  const [selectedSite, setSelectedSite] = useState<Site | null>(null);
  const router = useRouter();

  useEffect(() => {
    if (!mapContainerRef.current) return;

    const map = new mapboxgl.Map({
      container: mapContainerRef.current,
      style: 'mapbox://styles/mapbox/dark-v10',
      center: [-96, 40.8],
      zoom: 3.5,
      attributionControl: false,
      scrollZoom: false,
    });

    sites.forEach((site) => {
      const el = document.createElement('div');
      el.className = 'marker';
      el.style.backgroundImage = 'url(/green-energy-icon.svg)';
      el.style.width = '30px';
      el.style.height = '30px';
      el.style.backgroundSize = 'cover';
      el.style.cursor = 'pointer';
      el.style.borderRadius = '50%';
      el.style.transition = 'width 0.2s ease-in-out, height 0.2s ease-in-out';

      // Hover effects - use width/height instead of transform to avoid positioning issues
      el.addEventListener('mouseenter', () => {
        el.style.width = '33px';
        el.style.height = '33px';
      });

      el.addEventListener('mouseleave', () => {
        el.style.width = '30px';
        el.style.height = '30px';
      });

      // Click handler
      el.addEventListener('click', () => {
        setSelectedSite(site);
      });

      new mapboxgl.Marker(el)
        .setLngLat([site.locationLng, site.locationLat])
        .addTo(map);
    });

    return () => map.remove();
  }, [sites]);

  const handleOpenEMS = () => {
    if (selectedSite) {
      router.push(`/ems?siteId=${selectedSite.id}`);
    }
  };

  const handleClosePopup = () => {
    setSelectedSite(null);
  };

  return (
    <div className="relative h-full w-full">
      <div ref={mapContainerRef} style={{ height: '100%', width: '100%' }} />
      
      {/* Popup Card */}
      {selectedSite && (
        <div className="absolute inset-0 flex items-center justify-center bg-black/50 backdrop-blur-sm z-10">
          <div className="bg-slate-800/50 p-6 rounded-xl border border-green-500/20 backdrop-blur max-w-md w-full mx-4 relative">
            {/* Close button positioned at top-right */}
            <button
              onClick={handleClosePopup}
              className="absolute top-4 right-4 text-slate-400 hover:text-white transition-colors cursor-pointer"
            >
              <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
            
            <div className="flex items-center gap-4 mb-4">
              <div className="p-3 bg-green-500/10 rounded-lg">
                <svg
                  className="w-6 h-6 text-green-400"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                  />
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
              </div>
              <div className="flex-1">
                <h3 className="text-green-400 text-sm font-semibold">
                  EMS Site
                </h3>
                <p className="text-xl text-white font-bold">
                  {selectedSite.name}
                </p>
              </div>
            </div>
            
            <div className="space-y-3 mb-4">
              <div className="flex justify-between text-sm">
                <span className="text-green-400">Site ID:</span>
                <span className="text-white">{selectedSite.id}</span>
              </div>
              
              {selectedSite.contactPerson && (
                <div className="flex justify-between text-sm">
                  <span className="text-green-400">Contact:</span>
                  <span className="text-white">{selectedSite.contactPerson}</span>
                </div>
              )}
              
              {selectedSite.contactPhone && (
                <div className="flex justify-between text-sm">
                  <span className="text-green-400">Phone:</span>
                  <span className="text-white">{selectedSite.contactPhone}</span>
                </div>
              )}
              
              {selectedSite.contactEmail && (
                <div className="flex justify-between text-sm">
                  <span className="text-green-400">Email:</span>
                  <span className="text-white">{selectedSite.contactEmail}</span>
                </div>
              )}
            </div>
            
            {/* Primary action button positioned bottom-right */}
            <div className="flex justify-end pt-5">
              <button
                onClick={handleOpenEMS}
                className="bg-green-500 hover:bg-green-600 text-white py-2 px-4 rounded-lg transition-colors font-medium cursor-pointer"
              >
                Open EMS
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MapView;