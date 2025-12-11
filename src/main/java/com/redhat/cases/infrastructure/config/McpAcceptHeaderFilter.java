package com.redhat.cases.infrastructure.config;

import io.quarkus.vertx.http.runtime.filters.Filters;
import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Filtro HTTP que normaliza el header Accept para el endpoint MCP Streamable HTTP.
 *
 * El endpoint /mcp requiere estrictamente los headers:
 * - application/json
 * - text/event-stream
 *
 * Este filtro permite que clientes que envian Accept: wildcard puedan conectarse
 * sin modificar su configuracion.
 *
 * Configuracion:
 * - mcp.accept-header-filter.enabled=true (default: true)
 */
@ApplicationScoped
public class McpAcceptHeaderFilter {

    private static final Logger LOG = Logger.getLogger(McpAcceptHeaderFilter.class);
    private static final String MCP_ACCEPT_HEADER = "application/json, text/event-stream";

    @ConfigProperty(name = "mcp.accept-header-filter.enabled", defaultValue = "true")
    boolean enabled;

    public void registerFilter(@Observes Filters filters) {
        if (!enabled) {
            LOG.info("MCP Accept header filter is disabled");
            return;
        }

        LOG.info("MCP Accept header filter is enabled");
        filters.register(ctx -> {
            HttpServerRequest request = ctx.request();
            String path = request.path();

            // Solo aplicar al endpoint /mcp (Streamable HTTP), no a /mcp/sse
            if ("/mcp".equals(path)) {
                String accept = request.getHeader("Accept");

                // Si no tiene Accept o es */*, reemplazar por el header correcto
                if (accept == null || accept.contains("*/*")) {
                    request.headers().set("Accept", MCP_ACCEPT_HEADER);
                    LOG.debugf("Accept header normalizado para MCP Streamable HTTP: %s -> %s",
                            accept, MCP_ACCEPT_HEADER);
                }
            }

            ctx.next();
        }, 100); // Prioridad alta (se ejecuta antes que el handler MCP)
    }
}
