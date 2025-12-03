package com.redhat.cases.infrastructure.config;

import java.util.List;
import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuracion centralizada para la API de Red Hat.
 * Todos los valores se obtienen de application.properties.
 */
@ConfigMapping(prefix = "redhat.api")
public interface RedHatApiConfig {

    /**
     * Token offline de Red Hat para autenticacion.
     */
    Optional<String> offlineToken();

    /**
     * Configuracion de SSO.
     */
    Sso sso();

    /**
     * Configuracion de la API de casos.
     */
    Cases cases();

    /**
     * Timeouts de conexion.
     */
    Timeouts timeouts();

    /**
     * Lista de productos disponibles.
     */
    List<String> products();

    /**
     * Verifica si el servicio esta configurado correctamente.
     */
    default boolean isConfigured() {
        return offlineToken().isPresent() &&
               !offlineToken().get().isBlank() &&
               !offlineToken().get().equals("your-offline-token-here");
    }

    interface Sso {
        @WithDefault("https://sso.redhat.com/auth/realms/redhat-external/protocol/openid-connect/token")
        String tokenUrl();

        @WithDefault("rhsm-api")
        String clientId();

        @WithDefault("60")
        int tokenRenewalBufferSeconds();
    }

    interface Cases {
        @WithDefault("https://api.access.redhat.com/support")
        String baseUrl();

        @WithDefault("100")
        int maxResults();

        @WithDefault("lastModifiedDate")
        String defaultSortField();

        @WithDefault("DESC")
        String defaultSortOrder();
    }

    interface Timeouts {
        @WithDefault("30")
        int connectSeconds();

        @WithDefault("60")
        int requestSeconds();
    }
}
