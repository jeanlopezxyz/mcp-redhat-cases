package com.redhat.cases.infrastructure.redhat.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.cases.infrastructure.config.RedHatApiConfig;
import com.redhat.cases.infrastructure.redhat.dto.KnowledgeBaseArticleDto;
import com.redhat.cases.infrastructure.redhat.dto.KnowledgeBaseSearchResponseDto;

import static com.redhat.cases.infrastructure.redhat.RedHatApiConstants.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Cliente HTTP para el Knowledge Base de Red Hat (Hydra API).
 * Permite buscar articulos, soluciones y documentacion tecnica.
 */
@ApplicationScoped
public class KnowledgeBaseClient {

    private static final String HYDRA_BASE_URL = "https://access.redhat.com/hydra/rest/search/kcs";

    // Campos para busqueda simple
    private static final String SEARCH_FIELDS = "id,title,abstract,documentKind,view_uri,product,lastModifiedDate";

    // Campos para detalle completo de solucion
    private static final String DETAIL_FIELDS = "id,title,abstract,documentKind,view_uri,product,issue," +
            "solution_environment,solution_rootcause,solution_resolution,solution_diagnosticsteps," +
            "lastModifiedDate,createdDate";

    private final RedHatApiConfig config;
    private final RedHatAuthClient authClient;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Inject
    public KnowledgeBaseClient(RedHatApiConfig config, RedHatAuthClient authClient, ObjectMapper objectMapper) {
        this.config = config;
        this.authClient = authClient;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.timeouts().connectSeconds()))
                .build();
    }

    /**
     * Busca articulos en el Knowledge Base de Red Hat.
     *
     * @param query Termino de busqueda
     * @param maxResults Numero maximo de resultados (default 10)
     * @param product Filtrar por producto (opcional)
     * @return Lista de articulos encontrados
     */
    public List<KnowledgeBaseArticleDto> search(String query, int maxResults, String product) {
        return search(query, maxResults, product, null);
    }

    /**
     * Busca articulos en el Knowledge Base de Red Hat con filtro por tipo.
     *
     * @param query Termino de busqueda
     * @param maxResults Numero maximo de resultados (default 10)
     * @param product Filtrar por producto (opcional)
     * @param documentType Filtrar por tipo: Solution, Documentation, Article (opcional)
     * @return Lista de articulos encontrados
     */
    public List<KnowledgeBaseArticleDto> search(String query, int maxResults, String product, String documentType) {
        try {
            String token = authClient.getAccessToken();

            StringBuilder urlBuilder = new StringBuilder(HYDRA_BASE_URL);
            urlBuilder.append("?q=").append(URLEncoder.encode(query, StandardCharsets.UTF_8));
            urlBuilder.append("&rows=").append(maxResults > 0 ? maxResults : 10);
            urlBuilder.append("&fl=").append(SEARCH_FIELDS);

            if (product != null && !product.isBlank()) {
                urlBuilder.append("&fq=product:").append(URLEncoder.encode("\"" + product + "\"", StandardCharsets.UTF_8));
            }

            if (documentType != null && !documentType.isBlank()) {
                urlBuilder.append("&fq=documentKind:").append(URLEncoder.encode("\"" + documentType + "\"", StandardCharsets.UTF_8));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlBuilder.toString()))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .GET()
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK) {
                KnowledgeBaseSearchResponseDto searchResponse =
                    objectMapper.readValue(response.body(), KnowledgeBaseSearchResponseDto.class);
                return searchResponse.getResponse() != null
                    ? searchResponse.getResponse().getDocs()
                    : List.of();
            } else {
                throw new RuntimeException("Error buscando en Knowledge Base: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con Hydra API", e);
        }
    }

    /**
     * Obtiene el detalle completo de una solucion por su ID.
     *
     * @param solutionId ID de la solucion (ej: "5049001")
     * @return Detalle de la solucion o empty si no existe
     */
    public Optional<KnowledgeBaseArticleDto> getSolution(String solutionId) {
        try {
            String token = authClient.getAccessToken();

            String url = HYDRA_BASE_URL +
                "?q=" + URLEncoder.encode("id:" + solutionId, StandardCharsets.UTF_8) +
                "&fl=" + DETAIL_FIELDS;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header(HEADER_AUTHORIZATION, BEARER_PREFIX + token)
                    .GET()
                    .timeout(Duration.ofSeconds(config.timeouts().requestSeconds()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_OK) {
                KnowledgeBaseSearchResponseDto searchResponse =
                    objectMapper.readValue(response.body(), KnowledgeBaseSearchResponseDto.class);

                if (searchResponse.getResponse() != null &&
                    searchResponse.getResponse().getDocs() != null &&
                    !searchResponse.getResponse().getDocs().isEmpty()) {
                    return Optional.of(searchResponse.getResponse().getDocs().get(0));
                }
                return Optional.empty();
            } else {
                throw new RuntimeException("Error obteniendo solucion: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error conectando con Hydra API", e);
        }
    }
}
