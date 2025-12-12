package com.redhat.cases.client;

/**
 * API path constants for Red Hat Support API.
 * These are part of the API contract, not user configuration.
 *
 * For standard HTTP constants, use:
 * - jakarta.ws.rs.core.Response.Status (HTTP status codes)
 * - jakarta.ws.rs.core.HttpHeaders (header names)
 * - jakarta.ws.rs.core.MediaType (content types)
 */
public final class RedHatApiConstants {

    private RedHatApiConstants() {}

    // ========== Cases API Paths ==========
    public static final String PATH_CASES_FILTER = "/v1/cases/filter";
    public static final String PATH_CASES = "/v1/cases";
    public static final String PATH_CASE_BY_NUMBER = "/v1/cases/%s";
    public static final String PATH_CASE_COMMENTS = "/v1/cases/%s/comments";

    // ========== Account API Paths ==========
    public static final String PATH_CURRENT_ACCOUNT = "/v1/accounts/current";
    public static final String PATH_ENTITLEMENTS = "/v1/entitlements";

    // ========== OAuth ==========
    public static final String GRANT_TYPE_REFRESH = "refresh_token";
    public static final String BEARER_PREFIX = "Bearer ";
}
