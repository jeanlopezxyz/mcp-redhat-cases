package com.redhat.cases.infrastructure.redhat.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.cases.infrastructure.config.RedHatApiConfig;
import com.redhat.cases.infrastructure.redhat.dto.*;

import java.util.Collections;

import static com.redhat.cases.infrastructure.redhat.RedHatApiConstants.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Cliente HTTP para la API de Casos de Soporte de Red Hat.
 */
@ApplicationScoped
public class RedHatCasesClient {

    private final RedHatApiConfig config;
    private final RedHatAuthClient authClient;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Inject
    public RedHatCasesClient(RedHatApiConfig config, RedHatAuthClient authClient, ObjectMapper objectMapper) {
        this.config = config;
        this.authClient = authClient;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.timeouts().connectSeconds()))
                .build();
    }

    /**
     * Lista los casos de soporte con filtros opcionales.
     */
    public CaseListResponseDto listCases(CaseFilterRequestDto filter) {
        try {
            String token = authClient.getAccessToken();
            String jsonBody = objectMapper.writeValueAsString(filter);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.cases().baseUrl() + PATH_CASES_FILTER))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK) {
                return objectMapper.readValue(response.body(), CaseListResponseDto.class);
            } else {
                throw new RuntimeException("Error listando casos: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con API de Red Hat", e);
        }
    }

    /**
     * Obtiene un caso por su numero.
     */
    public CaseDetailDto getCase(String caseNumber) {
        try {
            String token = authClient.getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.cases().baseUrl() + String.format(PATH_CASE_BY_NUMBER, caseNumber)))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .GET()
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK) {
                return objectMapper.readValue(response.body(), CaseDetailDto.class);
            } else if (response.statusCode() == HTTP_NOT_FOUND) {
                return null;
            } else {
                throw new RuntimeException("Error obteniendo caso: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con API de Red Hat", e);
        }
    }

    /**
     * Crea un nuevo caso de soporte.
     * La API devuelve CaseLocation con la URI del caso creado.
     * Luego obtenemos los detalles completos del caso.
     */
    public CaseDetailDto createCase(CreateCaseRequestDto caseRequest) {
        try {
            String token = authClient.getAccessToken();
            String jsonBody = objectMapper.writeValueAsString(caseRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.cases().baseUrl() + PATH_CASES))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK || response.statusCode() == HTTP_CREATED) {
                // La API devuelve CaseLocation con la URI del caso creado
                CaseLocationDto location = objectMapper.readValue(response.body(), CaseLocationDto.class);
                String caseNumber = location.extractCaseNumber();

                if (caseNumber == null) {
                    throw new RuntimeException("No se pudo extraer el número de caso de la respuesta: " + response.body());
                }

                // Obtener los detalles completos del caso creado
                return getCase(caseNumber);
            } else {
                throw new RuntimeException("Error creando caso (HTTP " + response.statusCode() + "): " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con API de Red Hat: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza un caso existente.
     */
    public CaseDetailDto updateCase(String caseNumber, UpdateCaseRequestDto updateRequest) {
        try {
            String token = authClient.getAccessToken();
            String jsonBody = objectMapper.writeValueAsString(updateRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.cases().baseUrl() + String.format(PATH_CASE_BY_NUMBER, caseNumber)))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Success: 200 with body, 200 with empty body, or 204 No Content
            if (response.statusCode() == HTTP_OK || response.statusCode() == HTTP_NO_CONTENT) {
                String body = response.body();
                if (body != null && !body.isBlank()) {
                    return objectMapper.readValue(body, CaseDetailDto.class);
                }
                // Empty response on success - fetch the updated case
                return getCase(caseNumber);
            } else {
                throw new RuntimeException("Error actualizando caso (HTTP " + response.statusCode() + "): " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con API de Red Hat: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene los comentarios de un caso.
     */
    public List<CaseCommentDto> getComments(String caseNumber) {
        try {
            String token = authClient.getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.cases().baseUrl() + String.format(PATH_CASE_COMMENTS, caseNumber)))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .GET()
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK) {
                return objectMapper.readValue(response.body(), new TypeReference<List<CaseCommentDto>>() {});
            } else {
                throw new RuntimeException("Error obteniendo comentarios: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con API de Red Hat", e);
        }
    }

    /**
     * Agrega un comentario a un caso.
     */
    public CaseCommentDto addComment(String caseNumber, AddCommentRequestDto commentRequest) {
        try {
            String token = authClient.getAccessToken();
            String jsonBody = objectMapper.writeValueAsString(commentRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.cases().baseUrl() + String.format(PATH_CASE_COMMENTS, caseNumber)))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK || response.statusCode() == HTTP_CREATED) {
                return objectMapper.readValue(response.body(), CaseCommentDto.class);
            } else {
                throw new RuntimeException("Error agregando comentario: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con API de Red Hat", e);
        }
    }

    /**
     * Obtiene informacion de la cuenta actual.
     */
    public AccountInfoDto getCurrentAccount() {
        try {
            String token = authClient.getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.cases().baseUrl() + PATH_CURRENT_ACCOUNT))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .GET()
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK) {
                return objectMapper.readValue(response.body(), AccountInfoDto.class);
            } else {
                throw new RuntimeException("Error obteniendo cuenta: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con API de Red Hat", e);
        }
    }

    /**
     * Obtiene los entitlements (suscripciones) del usuario.
     * Los entitlements determinan para qué productos se pueden crear casos.
     */
    public List<EntitlementDto> getEntitlements() {
        try {
            String token = authClient.getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.cases().baseUrl() + PATH_ENTITLEMENTS))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .GET()
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK) {
                return objectMapper.readValue(response.body(), new TypeReference<List<EntitlementDto>>() {});
            } else {
                throw new RuntimeException("Error obteniendo entitlements (HTTP " + response.statusCode() + "): " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con API de Red Hat: " + e.getMessage(), e);
        }
    }
}
