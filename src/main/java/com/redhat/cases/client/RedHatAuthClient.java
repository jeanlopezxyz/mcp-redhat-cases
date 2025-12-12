package com.redhat.cases.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.cases.config.RedHatApiConfig;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Cliente para autenticacion con Red Hat SSO.
 * Obtiene y renueva tokens de acceso usando el offline token.
 */
@ApplicationScoped
public class RedHatAuthClient {

    private final RedHatApiConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private String cachedAccessToken;
    private Instant tokenExpiry;

    @Inject
    public RedHatAuthClient(RedHatApiConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.timeouts().connectSeconds()))
                .build();
    }

    /**
     * Obtiene un token de acceso valido.
     * Si el token cacheado esta expirado o no existe, obtiene uno nuevo.
     */
    public String getAccessToken() {
        if (cachedAccessToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedAccessToken;
        }
        return refreshAccessToken();
    }

    /**
     * Refresca el token de acceso usando el offline token.
     */
    private String refreshAccessToken() {
        try {
            String offlineToken = config.offlineToken()
                    .orElseThrow(() -> new RuntimeException("Offline token no configurado"));

            String requestBody = String.format(
                    "grant_type=refresh_token&client_id=%s&refresh_token=%s",
                    config.sso().clientId(),
                    offlineToken
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.sso().tokenUrl()))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == Response.Status.OK.getStatusCode()) {
                JsonNode json = objectMapper.readTree(response.body());
                cachedAccessToken = json.get("access_token").asText();
                int expiresIn = json.get("expires_in").asInt();
                tokenExpiry = Instant.now().plusSeconds(expiresIn - config.sso().tokenRenewalBufferSeconds());
                return cachedAccessToken;
            } else {
                throw new RuntimeException("Error obteniendo token de Red Hat SSO: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error en autenticacion con Red Hat SSO", e);
        }
    }

    /**
     * Verifica si el servicio esta configurado correctamente.
     */
    public boolean isConfigured() {
        return config.isConfigured();
    }
}
