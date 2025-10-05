"use client";

import React, { useEffect, useRef } from "react";
import mapboxgl from "mapbox-gl";

mapboxgl.accessToken =
    "pk.eyJ1Ijoia2dvbmlkaXMiLCJhIjoiY2tlY3hyZHBkMGdidjJxcDNyanhqZmF0ZCJ9.fpUmRv0Mb7XwB7cYojQGiA"; // Replace with your Mapbox token

type ClusterMapViewProps = {
    sites: {
        lat: number;
        lng: number;
        name: string;
        soc: number;
        onClick: () => void;
    }[];
};

const ClusterMapView: React.FC<ClusterMapViewProps> = ({ sites }) => {
    const mapContainerRef = useRef<HTMLDivElement | null>(null);
    const mapRef = useRef<mapboxgl.Map | null>(null);
    const sitesRef = useRef(sites);

    // Keep latest sites in a ref for handlers
    useEffect(() => {
        sitesRef.current = sites;
    }, [sites]);

    // Ensure mapbox CSS is loaded and initialize map once
    useEffect(() => {
        // Inject Mapbox GL CSS if it's not already present (runtime fallback)
        if (typeof document !== 'undefined') {
            const existing = Array.from(document.getElementsByTagName('link')).some((l) =>
                l.href && l.href.includes('mapbox-gl')
            );
            if (!existing) {
                const link = document.createElement('link');
                link.rel = 'stylesheet';
                link.href = 'https://api.mapbox.com/mapbox-gl-js/v2.15.0/mapbox-gl.css';
                document.head.appendChild(link);
            }
        }

        // Initialize map once
        
        if (!mapContainerRef.current) return;
        if (mapRef.current) return; // already initialized

        const map = new mapboxgl.Map({
            container: mapContainerRef.current,
            style: "mapbox://styles/mapbox/dark-v10",
            center: [-98.5795, 39.8283], // Center of the US
            zoom: 3,
            attributionControl: false,
        });

        mapRef.current = map;

        map.on("load", () => {
            const attributionControl = new mapboxgl.AttributionControl({
                compact: true,
            });
            map.addControl(attributionControl);

            const attributionElement = document.querySelector(
                ".mapboxgl-ctrl-attrib-inner"
            ) as HTMLElement | null;
            if (attributionElement) attributionElement.style.display = "none";

            // Add empty source and layers; data will be set via setData when sites change
            if (!map.getSource("sites")) {
                map.addSource("sites", {
                    type: "geojson",
                    data: {
                        type: "FeatureCollection",
                        features: [],
                    },
                } as mapboxgl.AnySourceData);
            }

            // heatmap layer
            if (!map.getLayer("heatmap")) {
                map.addLayer({
                    id: "heatmap",
                    type: "heatmap",
                    source: "sites",
                    paint: {
                        "heatmap-weight": [
                            "interpolate",
                            ["linear"],
                            ["get", "soc"],
                            0,
                            0,
                            100,
                            1,
                        ],
                        "heatmap-intensity": [
                            "interpolate",
                            ["linear"],
                            ["zoom"],
                            0,
                            1,
                            9,
                            3,
                        ],
                        "heatmap-color": [
                            "interpolate",
                            ["linear"],
                            ["heatmap-density"],
                            0,
                            "rgba(0,128,0,0)",
                            0.2,
                            "rgb(144,238,144)",
                            0.4,
                            "rgb(34,139,34)",
                            0.6,
                            "rgb(0,100,0)",
                            0.8,
                            "rgb(0,128,0)",
                            1,
                            "rgb(0,64,0)",
                        ],
                        "heatmap-radius": [
                            "interpolate",
                            ["linear"],
                            ["zoom"],
                            0,
                            5,
                            9,
                            60,
                        ],
                    },
                } as mapboxgl.AnyLayer);
            }

            // invisible circle layer for click interactions
            if (!map.getLayer("heatmap-points")) {
                map.addLayer({
                    id: "heatmap-points",
                    type: "circle",
                    source: "sites",
                    paint: {
                        "circle-radius": 20,
                        "circle-color": "#ffffff",
                        "circle-opacity": 0,
                    },
                } as mapboxgl.AnyLayer);
            }

            // click handler uses latest sites from ref
            map.on("click", "heatmap-points", (e) => {
                const features = map.queryRenderedFeatures(e.point, {
                    layers: ["heatmap-points"],
                });
                if (features.length > 0) {
                    const clickedFeature = features[0];
                    const props = clickedFeature.properties as Record<string, unknown> | null;
                    const name = props ? (props.name as string | undefined) : undefined;
                    if (!name) return;
                    const site = sitesRef.current.find((s) => s.name === name);
                    if (site) site.onClick();
                }
            });
        });

        return () => {
            map.remove();
            mapRef.current = null;
        };
    }, []);

    // Update source data when sites change (no map re-init)
    useEffect(() => {
        const map = mapRef.current;
        if (!map) return;

        const updateSource = () => {
            const source = map.getSource("sites") as mapboxgl.GeoJSONSource | undefined;
            const data = {
                type: "FeatureCollection",
                features: sites.map((site) => ({
                    type: "Feature",
                    geometry: { type: "Point", coordinates: [site.lng, site.lat] },
                    properties: { name: site.name, soc: site.soc },
                })),
            } as GeoJSON.FeatureCollection<GeoJSON.Geometry>;

            if (source && typeof source.setData === "function") {
                try {
                    source.setData(data);
                } catch (err) {
                    console.warn('ClusterMapView: setData failed, recreating source', err);
                    if (map.getLayer("heatmap")) map.removeLayer("heatmap");
                    if (map.getLayer("heatmap-points")) map.removeLayer("heatmap-points");
                    if (map.getSource("sites")) map.removeSource("sites");
                    map.addSource("sites", { type: "geojson", data } as mapboxgl.AnySourceData);
                }
            } else {
                if (map.getSource("sites")) map.removeSource("sites");
                map.addSource("sites", { type: "geojson", data } as mapboxgl.AnySourceData);
            }
        };

        // If style is not yet loaded, wait for 'styledata' event before adding/updating sources
        const typedMap = map as mapboxgl.Map & { isStyleLoaded?: () => boolean };
        if (typeof typedMap.isStyleLoaded === 'function' && !typedMap.isStyleLoaded()) {
            const onStyleData = () => {
                updateSource();
                typedMap.off('styledata', onStyleData);
            };
            typedMap.on('styledata', onStyleData);
        } else {
            updateSource();
        }
    }, [sites]);

    return <div ref={mapContainerRef} className="h-full w-full rounded-lg" />;
};

export default ClusterMapView;
