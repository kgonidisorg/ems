package com.ecogrid.ems.shared.constants;

/**
 * API endpoint constants for EMS services
 */
public final class ApiConstants {

    // Base paths
    public static final String API_BASE = "/api";
    public static final String V1 = "/v1";

    // Auth endpoints
    public static final String AUTH_BASE = API_BASE + "/auth" + V1;
    public static final String AUTH_LOGIN = "/login";
    public static final String AUTH_REGISTER = "/register";
    public static final String AUTH_REFRESH = "/refresh";
    public static final String AUTH_LOGOUT = "/logout";

    // Device endpoints
    public static final String DEVICES_BASE = API_BASE + "/devices" + V1;
    public static final String DEVICES_LIST = "/list";
    public static final String DEVICES_CREATE = "/create";
    public static final String DEVICES_UPDATE = "/{id}";
    public static final String DEVICES_DELETE = "/{id}";

    // Analytics endpoints
    public static final String ANALYTICS_BASE = API_BASE + "/analytics" + V1;
    public static final String ANALYTICS_DASHBOARD = "/dashboard";
    public static final String ANALYTICS_REPORTS = "/reports";

    // Notification endpoints
    public static final String NOTIFICATIONS_BASE = API_BASE + "/notifications" + V1;
    public static final String NOTIFICATIONS_SEND = "/send";
    public static final String NOTIFICATIONS_LIST = "/list";

    private ApiConstants() {
        // Utility class
    }
}