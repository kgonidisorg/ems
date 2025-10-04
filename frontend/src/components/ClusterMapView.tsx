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

    useEffect(() => {
        if (!mapContainerRef.current) return;

        const map = new mapboxgl.Map({
            container: mapContainerRef.current,
            style: "mapbox://styles/mapbox/dark-v10",
            center: [-98.5795, 39.8283], // Center of the US
            zoom: 3,
            attributionControl: false, // Disable default attribution control
        });

        // Add heatmap layer
        map.on("load", () => {
            const attributionControl = new mapboxgl.AttributionControl({
                compact: true,
            });
            map.addControl(attributionControl);

            // Remove the class `mapboxgl-ctrl-attrib-inner` after the control is added
            const attributionElement = document.querySelector(
                ".mapboxgl-ctrl-attrib-inner"
            ) as HTMLElement;
            console.log("Attribution Element:", attributionElement);
            if (attributionElement) {
                attributionElement.style.display = "none";
            }

            map.addSource("sites", {
                type: "geojson",
                data: {
                    type: "FeatureCollection",
                    features: sites.map((site) => ({
                        type: "Feature",
                        geometry: {
                            type: "Point",
                            coordinates: [site.lng, site.lat],
                        },
                        properties: {
                            name: site.name,
                            soc: site.soc,
                        },
                    })),
                },
            });

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
                        "rgba(0,128,0,0)", // Transparent green
                        0.2,
                        "rgb(144,238,144)", // Light green
                        0.4,
                        "rgb(34,139,34)", // Forest green
                        0.6,
                        "rgb(0,100,0)", // Dark green
                        0.8,
                        "rgb(0,128,0)", // Green
                        1,
                        "rgb(0,64,0)", // Deep green
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
            });

            map.addLayer({
                id: "heatmap-points",
                type: "circle",
                source: "sites",
                paint: {
                    "circle-radius": 20,
                    "circle-color": "#ffffff",
                    "circle-opacity": 0,
                },
            });

            map.on("click", "heatmap-points", (e) => {
                const features = map.queryRenderedFeatures(e.point, {
                    layers: ["heatmap-points"],
                });
                if (features.length > 0) {
                    const clickedFeature = features[0];
                    console.log("Clicked feature:", clickedFeature);

                    // Trigger the onClick handler passed via props
                    const site = sites.find(
                        (site) => site.name === clickedFeature.properties?.name
                    );
                    if (site) {
                        site.onClick();
                        console.log(
                            `Clicked on site: ${site.name}, SOC: ${site.soc}`
                        );
                    }
                }
            });
        });

        return () => map.remove();
    }, [sites]);

    return <div ref={mapContainerRef} className="h-full w-full rounded-lg" />;
};

export default ClusterMapView;
