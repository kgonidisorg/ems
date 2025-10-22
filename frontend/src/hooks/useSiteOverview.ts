import { useState, useEffect, useCallback, useRef, use } from "react";
import { Client, IMessage, StompSubscription } from "@stomp/stompjs";
import { SiteOverview } from "@/lib/types";
import { API_CONFIG, SiteService } from "@/lib/api";

interface UseSiteOverviewOptions {
    siteId: number | null;
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
}: UseSiteOverviewOptions): UseSiteOverviewReturn {
    const [data, setData] = useState<SiteOverview | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const siteIdRef = useRef<number | null>(siteId);
    const stompClientRef = useRef<Client | null>(null);

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
    const handleStompMessage = useCallback(
        (message: IMessage) => {
            try {
                const payload = JSON.parse(message.body);
                // Check if siteId and device id match
                if (
                    payload.siteId === siteIdRef.current &&
                    typeof payload.deviceId === "number"
                ) {
                    setData((prev) => {
                        if (!prev) return prev;
                        if (
                            !prev.devices?.some(
                                (d) => d.id === payload.deviceId
                            )
                        )
                            return prev;
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
        },
        [siteIdRef]
    );

    // Fetch data when siteId changes
    useEffect(() => {
        fetchSiteOverview();
        siteIdRef.current = siteId;
    }, [fetchSiteOverview, siteId]);

    // Create and activate the client only once
    useEffect(() => {
        if (!stompClientRef.current) {
            const client = new Client({
                brokerURL: API_CONFIG.WEBSOCKET_URL,
                webSocketFactory: () => new WebSocket(API_CONFIG.WEBSOCKET_URL),
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
            });
            client.onStompError = (frame) => {
                console.error(
                    "[STOMP ERROR] Broker error:",
                    frame.headers["message"],
                    frame.body
                );
            };
            client.onConnect = (frame) => {
                client.subscribe("/topic/telemetry", handleStompMessage);
            };
            client.activate();
            stompClientRef.current = client;
        }
        return () => {};
    }, [handleStompMessage]);

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
