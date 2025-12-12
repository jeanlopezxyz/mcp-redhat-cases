package com.redhat.cases.mcp;

import com.redhat.cases.service.ProductService;
import com.redhat.cases.service.SupportCaseService;
import com.redhat.cases.model.SupportCase;
import com.redhat.cases.dto.AccountInfoDto;
import com.redhat.cases.dto.EntitlementDto;
import com.redhat.cases.dto.ProductDto;
import com.redhat.cases.dto.VersionDto;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolResponse;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MCP Tools for Red Hat Support Case Management.
 *
 * This server provides 10 tools organized in 3 categories:
 *
 * ACCOUNT:
 * - getAccountInfo: Verify API connection and view account details
 * - getEntitlements: View subscriptions and which products allow case creation
 *
 * CASE MANAGEMENT:
 * - createCase: Open a new support case (requires product and version)
 * - getCase: Get full details of a specific case by number
 * - searchCases: List and filter cases (open/closed, by priority, product, status)
 * - updateCase: Change case status (reopen, close) or reassign contact
 * - addComment: Add information or updates to an existing case
 * - getStatistics: View case metrics and distribution
 *
 * PRODUCTS:
 * - listProducts: Get available Red Hat products for case creation
 * - listVersions: Get available versions for a specific product
 */
@ApplicationScoped
public class SupportCaseTools {

    @Inject
    SupportCaseService caseService;

    @Inject
    ProductService productService;

    // ========== ACCOUNT ==========

    @Tool(description = "Verify Red Hat API connection and get account information. " +
            "Use this first to confirm the service is configured correctly. " +
            "Returns: account name, number, status, region and country.")
    Uni<ToolResponse> getAccountInfo() {
        return Uni.createFrom().item(() -> {
            if (!caseService.isConfigured()) {
                return ToolResponse.error("Service is not configured. Set the REDHAT_TOKEN environment variable with your access token.\n" +
                       "You can generate a token at: https://access.redhat.com/management/api");
            }

            try {
                AccountInfoDto account = caseService.getCurrentAccount();
                if (account == null) {
                    return ToolResponse.error("Could not retrieve account information.");
                }

                StringBuilder sb = new StringBuilder();
                sb.append("=== Red Hat Account ===\n\n");
                sb.append("Name: ").append(account.getName()).append("\n");
                sb.append("Account Number: ").append(account.getAccountNumber()).append("\n");
                if (account.getAccountAlias() != null) {
                    sb.append("Alias: ").append(account.getAccountAlias()).append("\n");
                }
                sb.append("Status: ").append(account.getIsActive() != null && account.getIsActive() ? "Active" : "Unknown").append("\n");
                if (account.getSuperRegion() != null) {
                    sb.append("Region: ").append(account.getSuperRegion()).append("\n");
                }
                if (account.getIdentifyingAddressCountry() != null) {
                    sb.append("Country: ").append(account.getIdentifyingAddressCountry()).append("\n");
                }
                sb.append("\nRed Hat API Connection: OK");

                return ToolResponse.success(new TextContent(sb.toString()));
            } catch (Exception e) {
                return ToolResponse.error("ERROR connecting to Red Hat API: " + e.getMessage());
            }
        });
    }

    @Tool(description = "Get the user's Red Hat subscriptions (entitlements). " +
            "Shows which products you have access to for creating support cases. " +
            "Important: Only products with PREMIUM, STANDARD, or similar support levels " +
            "(not SELF-SUPPORTED) can be used to create support cases. " +
            "Use this BEFORE createCase to know which products are available.")
    Uni<ToolResponse> getEntitlements() {
        return Uni.createFrom().item(() -> {
            if (!caseService.isConfigured()) {
                return ToolResponse.error("Service is not configured. Set the REDHAT_TOKEN environment variable.");
            }

            try {
                List<EntitlementDto> entitlements = caseService.getEntitlements();

                if (entitlements.isEmpty()) {
                    return ToolResponse.success(new TextContent(
                        "No entitlements found for this account.\n\n" +
                        "To create support cases, you need an active Red Hat subscription " +
                        "with support (not Self-Support)."));
                }

                // Separate entitlements that allow case creation from self-support
                List<EntitlementDto> withSupport = entitlements.stream()
                    .filter(EntitlementDto::allowsCaseCreation)
                    .toList();
                List<EntitlementDto> selfSupport = entitlements.stream()
                    .filter(e -> !e.allowsCaseCreation())
                    .toList();

                StringBuilder sb = new StringBuilder();
                sb.append("=== Red Hat Entitlements ===\n\n");

                if (!withSupport.isEmpty()) {
                    sb.append("** Products WITH Support (can create cases): **\n");
                    for (EntitlementDto e : withSupport) {
                        sb.append(e.toString()).append("\n");
                    }
                    sb.append("\n");
                }

                if (!selfSupport.isEmpty()) {
                    sb.append("** Self-Support (NO case creation): **\n");
                    for (EntitlementDto e : selfSupport) {
                        sb.append(e.toString()).append("\n");
                    }
                    sb.append("\n");
                }

                sb.append("---\n");
                sb.append("Total: ").append(entitlements.size()).append(" entitlements\n");
                sb.append("With support: ").append(withSupport.size()).append("\n");
                sb.append("Self-support: ").append(selfSupport.size()).append("\n");

                if (withSupport.isEmpty()) {
                    sb.append("\nNOTE: You cannot create support cases with Self-Support subscriptions.\n");
                    sb.append("Consider upgrading to a subscription with support if you need case assistance.");
                }

                return ToolResponse.success(new TextContent(sb.toString()));
            } catch (Exception e) {
                return ToolResponse.error("ERROR getting entitlements: " + e.getMessage());
            }
        });
    }

    // ========== CASE MANAGEMENT ==========

    @Tool(description = "Create a new Red Hat support case. " +
            "Required: title, description, product (exact name from listProducts), version (from listVersions). " +
            "Optional: priority (LOW/NORMAL/HIGH/URGENT), reporter email. " +
            "Use listProducts and listVersions first to get valid values. " +
            "Returns: created case details with case number for tracking.")
    Uni<ToolResponse> createCase(
            @ToolArg(description = "Brief problem summary (max 255 chars)") String title,
            @ToolArg(description = "Detailed problem description: what happened, error messages, steps to reproduce, business impact") String description,
            @ToolArg(description = "Exact product name from listProducts (e.g. 'OpenShift Container Platform')") String product,
            @ToolArg(description = "Product version from listVersions (e.g. '4.14', '9.3')") String version,
            @ToolArg(description = "Case priority: LOW (questions), NORMAL (affecting operations), HIGH (critical functionality), URGENT (production down)", defaultValue = "NORMAL") String priority,
            @ToolArg(description = "Reporter email address", defaultValue = "") String reporter) {

        return Uni.createFrom().item(() -> {
            if (!caseService.isConfigured()) {
                return ToolResponse.error("Service is not configured. Use getAccountInfo to verify the configuration.");
            }

            if (title == null || title.isBlank()) {
                return ToolResponse.error("Title is required.");
            }
            if (description == null || description.isBlank()) {
                return ToolResponse.error("Description is required.");
            }
            if (product == null || product.isBlank()) {
                return ToolResponse.error("Product is required. Use listProducts to see available products.");
            }
            if (version == null || version.isBlank()) {
                return ToolResponse.error("Version is required. Use listVersions to see available versions for the product.");
            }

            try {
                SupportCase newCase = caseService.createCase(title, description, product, version, priority, reporter);
                return ToolResponse.success(new TextContent(
                    String.format("Case created successfully:\n%s", newCase.toDetailedString())));
            } catch (Exception e) {
                return ToolResponse.error("ERROR creating case: " + e.getMessage());
            }
        });
    }

    @Tool(description = "Get complete details of a support case by its number. " +
            "Returns: case status, priority, product, version, description, comments history, dates, and assigned contact. " +
            "Example: getCase caseNumber='03881234'")
    Uni<ToolResponse> getCase(@ToolArg(description = "8-digit Red Hat case number (e.g. '03881234')") String caseNumber) {
        return Uni.createFrom().item(() -> {
            if (!caseService.isConfigured()) {
                return ToolResponse.error("Service is not configured. Use getAccountInfo to verify the configuration.");
            }

            return caseService.getCase(caseNumber)
                    .map(c -> ToolResponse.success(new TextContent(c.toDetailedString())))
                    .orElse(ToolResponse.error("Case not found: " + caseNumber));
        });
    }

    @Tool(description = "Search and list support cases with optional filters. " +
            "Without filters: lists all open cases. " +
            "Filters: query (text search), status, priority, product, includeClosed. " +
            "Examples: " +
            "- List open cases: searchCases " +
            "- List ALL cases: searchCases includeClosed=true " +
            "- Find urgent cases: searchCases priority='URGENT' " +
            "- Find by product: searchCases product='OpenShift' " +
            "- Search by text: searchCases query='authentication error'")
    Uni<ToolResponse> searchCases(
            @ToolArg(description = "Text to search in title, description or case number", defaultValue = "") String query,
            @ToolArg(description = "Filter by status: 'Waiting on Red Hat', 'Waiting on Customer', 'Closed'", defaultValue = "") String status,
            @ToolArg(description = "Filter by priority: LOW, NORMAL, HIGH, URGENT", defaultValue = "") String priority,
            @ToolArg(description = "Filter by product name (e.g. 'OpenShift', 'RHEL')", defaultValue = "") String product,
            @ToolArg(description = "Include closed cases (default: false, only shows open cases)", defaultValue = "false") boolean includeClosed) {

        return Uni.createFrom().item(() -> {
            if (!caseService.isConfigured()) {
                return ToolResponse.error("Service is not configured. Use getAccountInfo to verify the configuration.");
            }

            List<SupportCase> results = caseService.searchCases(query, status, priority, product, includeClosed);

            if (results.isEmpty()) {
                String msg = (query.isEmpty() && status.isEmpty() && priority.isEmpty() && product.isEmpty())
                    ? (includeClosed ? "No support cases in this account." : "No active support cases in this account.")
                    : "No cases found with the specified criteria.";
                return ToolResponse.success(new TextContent(msg));
            }

            StringBuilder sb = new StringBuilder();
            String title = (query.isEmpty() && status.isEmpty() && priority.isEmpty() && product.isEmpty())
                ? (includeClosed ? "All Cases" : "Active Cases")
                : "Search Results";
            sb.append("=== ").append(title).append(" (").append(results.size()).append(") ===\n\n");

            for (SupportCase c : results) {
                sb.append(c.toString()).append("\n");
            }

            return ToolResponse.success(new TextContent(sb.toString()));
        });
    }

    @Tool(description = "Update a support case: change status or reassign contact. " +
            "Status values: 'Waiting on Red Hat' (reopen closed case), 'Waiting on Customer', 'Closed'. " +
            "At least one of status or contactSSOName must be provided. " +
            "Examples: " +
            "- Reopen closed case: updateCase caseNumber='03881234' status='Waiting on Red Hat' " +
            "- Close case: updateCase caseNumber='03881234' status='Closed' " +
            "- Reassign: updateCase caseNumber='03881234' contactSSOName='jsmith'")
    Uni<ToolResponse> updateCase(
            @ToolArg(description = "8-digit Red Hat case number") String caseNumber,
            @ToolArg(description = "New status: 'Waiting on Red Hat' (reopen), 'Waiting on Customer', 'Closed'", defaultValue = "") String status,
            @ToolArg(description = "SSO username of new primary contact", defaultValue = "") String contactSSOName) {

        return Uni.createFrom().item(() -> {
            if (!caseService.isConfigured()) {
                return ToolResponse.error("Service is not configured. Use getAccountInfo to verify the configuration.");
            }

            if (caseNumber == null || caseNumber.isBlank()) {
                return ToolResponse.error("Case number is required.");
            }

            boolean hasStatus = status != null && !status.isBlank();
            boolean hasContact = contactSSOName != null && !contactSSOName.isBlank();

            if (!hasStatus && !hasContact) {
                return ToolResponse.error("At least one field must be provided: status or contactSSOName.");
            }

            try {
                Optional<SupportCase> result = Optional.empty();

                if (hasStatus) {
                    result = caseService.updateStatus(caseNumber, status);
                }
                if (hasContact && (result.isPresent() || !hasStatus)) {
                    result = caseService.assignCase(caseNumber, contactSSOName);
                }

                return result
                    .map(c -> ToolResponse.success(new TextContent(
                        String.format("Case updated successfully:\n%s", c.toDetailedString()))))
                    .orElse(ToolResponse.error("Could not update the case. Verify that the case number is valid."));
            } catch (Exception e) {
                return ToolResponse.error("ERROR updating case: " + e.getMessage());
            }
        });
    }

    @Tool(description = "Add a comment to an existing support case. " +
            "Use to provide additional information, logs, updates, or respond to support team questions. " +
            "The comment will be visible in the case history.")
    Uni<ToolResponse> addComment(
            @ToolArg(description = "8-digit Red Hat case number") String caseNumber,
            @ToolArg(description = "Comment text: additional details, logs, or response to support") String comment,
            @ToolArg(description = "Your name or identifier") String author,
            @ToolArg(description = "Internal comment not visible to customer (default: false)", defaultValue = "false") boolean isInternal) {

        return Uni.createFrom().item(() -> {
            if (!caseService.isConfigured()) {
                return ToolResponse.error("Service is not configured. Use getAccountInfo to verify the configuration.");
            }

            return caseService.addComment(caseNumber, author, comment, isInternal)
                    .map(c -> ToolResponse.success(new TextContent(
                        String.format("Comment added successfully to case %s", caseNumber))))
                    .orElse(ToolResponse.error("Could not add comment. Verify that the case exists."));
        });
    }

    @Tool(description = "Get statistics and metrics for support cases. " +
            "Returns: total cases, urgent open cases, distribution by status, priority, and product. " +
            "Useful for dashboards and executive summaries.")
    Uni<ToolResponse> getStatistics() {
        return Uni.createFrom().item(() -> {
            if (!caseService.isConfigured()) {
                return ToolResponse.error("Service is not configured. Use getAccountInfo to verify the configuration.");
            }

            Map<String, Object> stats = caseService.getStatistics();

            if (stats.containsKey("error")) {
                return ToolResponse.error("ERROR: " + stats.get("error"));
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== Red Hat Case Statistics ===\n\n");
            sb.append("Total cases: ").append(stats.get("total")).append("\n");
            sb.append("Open urgent cases: ").append(stats.get("urgentOpen")).append("\n\n");

            sb.append("By Status:\n");
            @SuppressWarnings("unchecked")
            Map<String, Long> byStatus = (Map<String, Long>) stats.get("byStatus");
            if (byStatus != null) {
                byStatus.forEach((k, v) -> sb.append("  - ").append(k).append(": ").append(v).append("\n"));
            }

            sb.append("\nBy Priority:\n");
            @SuppressWarnings("unchecked")
            Map<String, Long> byPriority = (Map<String, Long>) stats.get("byPriority");
            if (byPriority != null) {
                byPriority.forEach((k, v) -> sb.append("  - ").append(k).append(": ").append(v).append("\n"));
            }

            sb.append("\nBy Product:\n");
            @SuppressWarnings("unchecked")
            Map<String, Long> byProduct = (Map<String, Long>) stats.get("byProduct");
            if (byProduct != null) {
                byProduct.forEach((k, v) -> sb.append("  - ").append(k).append(": ").append(v).append("\n"));
            }

            return ToolResponse.success(new TextContent(sb.toString()));
        });
    }

    // ========== PRODUCTS & VERSIONS ==========

    @Tool(description = "List all Red Hat products available for creating support cases. " +
            "Returns product names and codes. Use the exact product NAME when creating a case. " +
            "After finding your product, use listVersions to get available versions.")
    Uni<ToolResponse> listProducts() {
        return Uni.createFrom().item(() -> {
            if (!productService.isConfigured()) {
                return ToolResponse.error("Service is not configured. Use getAccountInfo to verify the configuration.");
            }

            try {
                List<ProductDto> products = productService.getProducts();

                if (products.isEmpty()) {
                    return ToolResponse.success(new TextContent("No products available."));
                }

                StringBuilder sb = new StringBuilder();
                sb.append("=== Red Hat Products (").append(products.size()).append(") ===\n\n");

                products.forEach(p -> {
                    sb.append("- ").append(p.getName());
                    sb.append(" [code: ").append(p.getCode()).append("]");
                    if (p.getLine() != null && !p.getLine().isBlank()) {
                        sb.append(" (").append(p.getLine()).append(")");
                    }
                    sb.append("\n");
                });

                sb.append("\nUse listVersions with the product code to see available versions.");

                return ToolResponse.success(new TextContent(sb.toString()));
            } catch (Exception e) {
                return ToolResponse.error("ERROR getting products: " + e.getMessage());
            }
        });
    }

    @Tool(description = "List available versions for a Red Hat product. " +
            "Use after listProducts to get valid versions for case creation. " +
            "Example: listVersions productCode='OpenShift Container Platform'")
    Uni<ToolResponse> listVersions(
            @ToolArg(description = "Product code from listProducts") String productCode) {
        return Uni.createFrom().item(() -> {
            if (!productService.isConfigured()) {
                return ToolResponse.error("Service is not configured. Use getAccountInfo to verify the configuration.");
            }

            if (productCode == null || productCode.isBlank()) {
                return ToolResponse.error("Product code is required. Use listProducts to see available products.");
            }

            try {
                List<VersionDto> versions = productService.getVersions(productCode);

                if (versions.isEmpty()) {
                    return ToolResponse.success(new TextContent(
                        "No versions found for product: " + productCode + "\n" +
                        "Make sure you're using the correct product code from listProducts."));
                }

                StringBuilder sb = new StringBuilder();
                sb.append("=== Versions for ").append(productCode).append(" (").append(versions.size()).append(") ===\n\n");

                versions.forEach(v -> {
                    sb.append("- ").append(v.getName());
                    if (Boolean.TRUE.equals(v.getIsDefault())) {
                        sb.append(" (default)");
                    }
                    if (v.getDescription() != null && !v.getDescription().isBlank()) {
                        sb.append(" - ").append(v.getDescription());
                    }
                    sb.append("\n");
                });

                return ToolResponse.success(new TextContent(sb.toString()));
            } catch (Exception e) {
                return ToolResponse.error("ERROR getting versions: " + e.getMessage());
            }
        });
    }

}
