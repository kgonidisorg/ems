import React, { useEffect, useRef } from 'react';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

mapboxgl.accessToken = 'pk.eyJ1Ijoia2dvbmlkaXMiLCJhIjoiY2tlY3hyZHBkMGdidjJxcDNyanhqZmF0ZCJ9.fpUmRv0Mb7XwB7cYojQGiA'; // Replace with your Mapbox token

const MapView: React.FC<{ sites: { lat: number; lng: number; name: string }[] }> = ({ sites }) => {
  const mapContainerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!mapContainerRef.current) return;

    const map = new mapboxgl.Map({
      container: mapContainerRef.current,
      style: 'mapbox://styles/mapbox/dark-v10',
      center: [-96, 40.8],
      zoom: 3.5,
      attributionControl: false, // Disable default attribution control
      scrollZoom: false, // Disable mousewheel scrolling
    });

    sites.forEach((site) => {
      const el = document.createElement('div');
      el.className = 'marker';
      el.style.backgroundImage = 'url(/green-energy-icon.svg)'; // Replace with actual path
      el.style.width = '30px';
      el.style.height = '30px';
      el.style.backgroundSize = 'cover';

      new mapboxgl.Marker(el)
        .setLngLat([site.lng, site.lat])
        .setPopup(new mapboxgl.Popup().setText(site.name))
        .addTo(map);
    });

    return () => map.remove();
  }, [sites]);

  return <div ref={mapContainerRef} style={{ height: '100%', width: '100%' }} />;
};

export default MapView;