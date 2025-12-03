package com.redhat.cases.infrastructure.redhat.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para la respuesta de creación de caso.
 * La API devuelve las URIs del caso creado.
 * Formato: { "location": ["https://access.redhat.com/hydra/rest/v1/cases/04324138"] }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseLocationDto {

    @JsonProperty("location")
    private List<String> location;

    public List<String> getLocation() { return location; }
    public void setLocation(List<String> location) { this.location = location; }

    /**
     * Extrae el número de caso de la primera URI.
     * Formato esperado: https://access.redhat.com/hydra/rest/v1/cases/04324138
     */
    public String extractCaseNumber() {
        if (location == null || location.isEmpty()) {
            return null;
        }
        String uri = location.get(0);
        // Extract case number from URI
        int lastSlash = uri.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < uri.length() - 1) {
            return uri.substring(lastSlash + 1);
        }
        return null;
    }
}
