package com.redhat.cases.infrastructure.redhat.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.cases.infrastructure.config.RedHatApiConfig;
import com.redhat.cases.infrastructure.redhat.dto.ProductDto;
import com.redhat.cases.infrastructure.redhat.dto.VersionDto;

import static com.redhat.cases.infrastructure.redhat.RedHatApiConstants.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Cliente HTTP para la API Hydra de Red Hat.
 * Proporciona información de productos y versiones disponibles.
 */
@ApplicationScoped
public class HydraClient {

    private static final String HYDRA_BASE_URL = "https://api.access.redhat.com/hydra/rest";
    private static final String PATH_PRODUCTS = "/products";
    private static final String PATH_PRODUCT_VERSIONS = "/products/%s/versions";

    private final RedHatApiConfig config;
    private final RedHatAuthClient authClient;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Inject
    public HydraClient(RedHatApiConfig config, RedHatAuthClient authClient, ObjectMapper objectMapper) {
        this.config = config;
        this.authClient = authClient;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.timeouts().connectSeconds()))
                .build();
    }

    /**
     * Obtiene la lista de productos disponibles para soporte.
     */
    public List<ProductDto> getProducts() {
        try {
            String token = authClient.getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HYDRA_BASE_URL + PATH_PRODUCTS))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .GET()
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK) {
                return objectMapper.readValue(response.body(), new TypeReference<List<ProductDto>>() {});
            } else {
                throw new RuntimeException("Error obteniendo productos (HTTP " + response.statusCode() + "): " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con Hydra API: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene las versiones disponibles para un producto.
     */
    public List<VersionDto> getProductVersions(String productCode) {
        try {
            String token = authClient.getAccessToken();
            String encodedProduct = java.net.URLEncoder.encode(productCode, "UTF-8");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HYDRA_BASE_URL + String.format(PATH_PRODUCT_VERSIONS, encodedProduct)))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .GET()
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK) {
                return objectMapper.readValue(response.body(), new TypeReference<List<VersionDto>>() {});
            } else if (response.statusCode() == HTTP_NOT_FOUND) {
                return Collections.emptyList();
            } else {
                throw new RuntimeException("Error obteniendo versiones (HTTP " + response.statusCode() + "): " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con Hydra API: " + e.getMessage(), e);
        }
    }
}
