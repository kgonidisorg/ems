import { useState, useEffect, useCallback } from "react";
import { Client, IMessage, IFrame } from "@stomp/stompjs";
import { SiteOverview } from "@/lib/types";
import { SiteService } from "@/lib/api";

interface UseSiteOverviewOptions {
    siteId: number | null;
    refreshInterval?: number; // Optional auto-refresh interval in milliseconds
}

interface UseSiteOverviewReturn {
    data: SiteOverview | null;
    loading: boolean;
    error: string | null;
    refetch: () => Promise<void>;
}

/**
 * Custom hook for fetching and managing site overview data
 *
 * @param options - Configuration options including siteId and optional refresh interval
 * @returns Object containing data, loading state, error state, and refetch function
 */
export function useSiteOverview({
    siteId,
    refreshInterval,
}: UseSiteOverviewOptions): UseSiteOverviewReturn {
    const [data, setData] = useState<SiteOverview | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    const fetchSiteOverview = useCallback(async () => {
        if (!siteId) {
            setData(null);
            setLoading(false);
            setError(null);
            return;
        }

        try {
            setLoading(true);
            setError(null);

            const overview = await SiteService.getSiteOverview(siteId);
            setData(overview);
        } catch (err) {
            const errorMessage =
                err instanceof Error ? err.message : "Unknown error occurred";
            setError(errorMessage);
            console.error("Error fetching site overview:", err);
        } finally {
            setLoading(false);
        }
    }, [siteId]);

    // Fetch data when siteId changes
    useEffect(() => {
        fetchSiteOverview();
    }, [fetchSiteOverview]);

    // Optional auto-refresh functionality
    useEffect(() => {
        if (!refreshInterval || !siteId) return;

        const interval = setInterval(() => {
            fetchSiteOverview();
        }, refreshInterval);

        return () => clearInterval(interval);
    }, [fetchSiteOverview, refreshInterval, siteId]);

    // STOMP WebSocket subscription for real-time telemetry
    useEffect(() => {
        if (!siteId) return;

        const stompClient = new Client({
            brokerURL: "ws://localhost:8080/ws",
            webSocketFactory: () => new WebSocket("ws://localhost:8080/ws"),
            debug: (str: string) => console.log("[STOMP DEBUG]", str),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        stompClient.onConnect = (frame: IFrame) => {
            stompClient.subscribe("/topic/telemetry", (message: IMessage) => {
                try {
                    const payload = JSON.parse(message.body);
                    // Check if siteId and device id match
                    if (
                        payload.siteId === siteId &&
                        typeof payload.deviceId === "number" &&
                        data &&
                        data.devices?.some((d) => d.id === payload.deviceId)
                    ) {
                        setData((prev) => {
                            if (!prev) return prev;
                            return {
                                ...prev,
                                devices:
                                    prev.devices?.map((device) => {
                                        if (device.id === payload.deviceId) {
                                            const prevTelemetry =
                                                device.latestTelemetry;
                                            return {
                                                ...device,
                                                latestTelemetry: prevTelemetry
                                                    ? {
                                                          ...prevTelemetry,
                                                          data: payload.telemetry,
                                                          timestamp:
                                                              payload.timestamp ||
                                                              prevTelemetry.timestamp,
                                                          telemetryType:
                                                              prevTelemetry.telemetryType ||
                                                              (typeof payload.deviceType ===
                                                              "string"
                                                                  ? payload.deviceType
                                                                  : "UNKNOWN"),
                                                      }
                                                    : {
                                                          timestamp:
                                                              payload.timestamp ||
                                                              "",
                                                          telemetryType:
                                                              typeof payload.deviceType ===
                                                              "string"
                                                                  ? payload.deviceType
                                                                  : "UNKNOWN",
                                                          data: payload.telemetry,
                                                      },
                                            };
                                        }
                                        return device;
                                    }) || [],
                            };
                        });
                    }
                } catch (err) {
                    console.error("[STOMP] Error parsing message:", err);
                }
            });
        };

        stompClient.onStompError = (frame: IFrame) => {
            console.error(
                "[STOMP ERROR] Broker error:",
                frame.headers["message"],
                frame.body
            );
        };

        stompClient.activate();

        return () => {
            stompClient.deactivate();
        };
    }, [siteId, data]);

    const refetch = useCallback(async () => {
        await fetchSiteOverview();
    }, [fetchSiteOverview]);

    return {
        data,
        loading,
        error,
        refetch,
    };
}
