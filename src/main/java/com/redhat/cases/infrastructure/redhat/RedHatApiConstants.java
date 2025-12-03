package com.redhat.cases.infrastructure.redhat;

/**
 * Constantes para la API de Red Hat Support.
 * Estos valores son parte del contrato de la API, no configuracion del usuario.
 */
public final class RedHatApiConstants {

    private RedHatApiConstants() {}

    // ========== API Paths ==========
    public static final String PATH_CASES_FILTER = "/v1/cases/filter";
    public static final String PATH_CASES = "/v1/cases";
    public static final String PATH_CASE_BY_NUMBER = "/v1/cases/%s";
    public static final String PATH_CASE_COMMENTS = "/v1/cases/%s/comments";
    public static final String PATH_CURRENT_ACCOUNT = "/v1/accounts/current";
    public static final String PATH_ENTITLEMENTS = "/v1/entitlements";

    // ========== HTTP Status Codes ==========
    public static final int HTTP_OK = 200;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_NO_CONTENT = 204;
    public static final int HTTP_NOT_FOUND = 404;

    // ========== HTTP Headers ==========
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String BEARER_PREFIX = "Bearer ";

    // ========== OAuth Grant Types ==========
    public static final String GRANT_TYPE_REFRESH = "refresh_token";
}
