package com.redhat.cases.adapter.mcp;

import io.quarkiverse.mcp.server.McpConnection;
import io.quarkiverse.mcp.server.Notification;
import io.quarkiverse.mcp.server.Notification.Type;
import io.quarkus.logging.Log;

/**
 * Handler for MCP client notifications.
 * Logs connection events.
 */
public class McpNotificationHandler {

    @Notification(Type.INITIALIZED)
    void onClientInitialized(McpConnection connection) {
        Log.infof("MCP client connected - Connection ID: %s", connection.id());
        Log.debugf("Client capabilities - Sampling: %s, Roots: %s",
            connection.initialRequest().supportsSampling(),
            connection.initialRequest().supportsRoots());
    }
}
