package com.redhat.cases.application.service;

import com.redhat.cases.domain.model.*;
import com.redhat.cases.infrastructure.config.RedHatApiConfig;
import com.redhat.cases.infrastructure.redhat.client.RedHatAuthClient;
import com.redhat.cases.infrastructure.redhat.client.RedHatCasesClient;
import com.redhat.cases.infrastructure.redhat.dto.*;
import com.redhat.cases.infrastructure.redhat.dto.EntitlementDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application service for Red Hat support case management.
 * Connects with the real Red Hat Support API and transforms DTOs to domain entities.
 */
@ApplicationScoped
public class SupportCaseService {

    private final RedHatApiConfig config;
    private final RedHatCasesClient casesClient;
    private final RedHatAuthClient authClient;

    @Inject
    public SupportCaseService(RedHatApiConfig config, RedHatCasesClient casesClient, RedHatAuthClient authClient) {
        this.config = config;
        this.casesClient = casesClient;
        this.authClient = authClient;
    }

    /**
     * Verifies if the service is correctly configured.
     */
    public boolean isConfigured() {
        return authClient.isConfigured();
    }

    /**
     * Creates a new support case.
     */
    public SupportCase createCase(String title, String description, String product,
                                  String version, String priority, String reporter) {
        if (!isConfigured()) {
            throw new RuntimeException("Service is not configured. Configure redhat.api.offline-token");
        }

        CreateCaseRequestDto request = new CreateCaseRequestDto();
        request.setSummary(title);
        request.setDescription(description);
        request.setProduct(product);
        request.setVersion(version);
        request.setSeverity(mapPriorityToSeverity(priority));

        CaseDetailDto created = casesClient.createCase(request);
        return mapToSupportCase(created);
    }

    /**
     * Gets a case by number.
     */
    public Optional<SupportCase> getCase(String caseNumber) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        CaseDetailDto detail = casesClient.getCase(caseNumber);
        if (detail == null) {
            return Optional.empty();
        }
        return Optional.of(mapToSupportCase(detail));
    }

    /**
     * Lists all cases.
     * @param includeClosed if true, includes closed cases
     */
    public List<SupportCase> listCases(boolean includeClosed) {
        if (!isConfigured()) {
            return Collections.emptyList();
        }

        CaseFilterRequestDto filter = new CaseFilterRequestDto(config.cases().maxResults());
        filter.setIncludeClosed(includeClosed);
        filter.setSortField(config.cases().defaultSortField());
        filter.setSortOrder(config.cases().defaultSortOrder());

        CaseListResponseDto response = casesClient.listCases(filter);
        if (response == null || response.getCases() == null) {
            return Collections.emptyList();
        }

        return response.getCases().stream()
            .map(this::mapToSupportCase)
            .collect(Collectors.toList());
    }

    /**
     * Lists all cases (only open by default).
     */
    public List<SupportCase> listCases() {
        return listCases(false);
    }

    /**
     * Searches cases by criteria.
     */
    public List<SupportCase> searchCases(String query, String status, String priority, String product, boolean includeClosed) {
        if (!isConfigured()) {
            return Collections.emptyList();
        }

        CaseFilterRequestDto filter = new CaseFilterRequestDto(config.cases().maxResults());
        filter.setIncludeClosed(includeClosed);
        filter.setSortField(config.cases().defaultSortField());
        filter.setSortOrder(config.cases().defaultSortOrder());

        if (query != null && !query.isEmpty()) {
            filter.setKeyword(query);
        }
        if (status != null && !status.isEmpty()) {
            filter.setStatus(mapStatusToApi(status));
        }
        if (priority != null && !priority.isEmpty()) {
            filter.setSeverity(mapPriorityToSeverity(priority));
        }
        if (product != null && !product.isEmpty()) {
            filter.setProduct(product);
        }

        CaseListResponseDto response = casesClient.listCases(filter);
        if (response == null || response.getCases() == null) {
            return Collections.emptyList();
        }

        return response.getCases().stream()
            .map(this::mapToSupportCase)
            .sorted(Comparator.comparing((SupportCase c) -> c.getPriority().getLevel())
                .thenComparing(SupportCase::getUpdatedAt).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Updates the status of a case.
     */
    public Optional<SupportCase> updateStatus(String caseNumber, String newStatus) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        UpdateCaseRequestDto request = new UpdateCaseRequestDto();
        request.setStatus(mapStatusToApi(newStatus));

        CaseDetailDto updated = casesClient.updateCase(caseNumber, request);
        if (updated == null) {
            return Optional.empty();
        }
        return Optional.of(mapToSupportCase(updated));
    }

    /**
     * Adds a comment to a case.
     */
    public Optional<SupportCase> addComment(String caseNumber, String author, String content, boolean isInternal) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        AddCommentRequestDto request = new AddCommentRequestDto(content);
        casesClient.addComment(caseNumber, request);

        return getCase(caseNumber);
    }

    /**
     * Updates the primary contact of a case.
     */
    public Optional<SupportCase> assignCase(String caseNumber, String assignee) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        UpdateCaseRequestDto request = new UpdateCaseRequestDto();
        request.setContactSSOName(assignee);

        CaseDetailDto updated = casesClient.updateCase(caseNumber, request);
        if (updated == null) {
            return Optional.empty();
        }
        return Optional.of(mapToSupportCase(updated));
    }

    /**
     * Gets the comments of a case.
     */
    public List<CaseComment> getComments(String caseNumber) {
        if (!isConfigured()) {
            return Collections.emptyList();
        }

        List<CaseCommentDto> comments = casesClient.getComments(caseNumber);
        if (comments == null) {
            return Collections.emptyList();
        }

        return comments.stream()
            .map(this::mapToComment)
            .collect(Collectors.toList());
    }

    /**
     * Gets case statistics.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();

        if (!isConfigured()) {
            stats.put("error", "Service not configured");
            return stats;
        }

        List<SupportCase> allCases = listCases();

        Map<String, Long> byStatus = allCases.stream()
            .collect(Collectors.groupingBy(c -> c.getStatus().getDisplayName(), Collectors.counting()));

        Map<String, Long> byPriority = allCases.stream()
            .collect(Collectors.groupingBy(c -> c.getPriority().getDisplayName(), Collectors.counting()));

        Map<String, Long> byProduct = allCases.stream()
            .filter(c -> c.getProduct() != null)
            .collect(Collectors.groupingBy(SupportCase::getProduct, Collectors.counting()));

        stats.put("total", allCases.size());
        stats.put("byStatus", byStatus);
        stats.put("byPriority", byPriority);
        stats.put("byProduct", byProduct);

        long urgent = allCases.stream()
            .filter(c -> c.getPriority() == CasePriority.URGENT &&
                        c.getStatus() != CaseStatus.RESOLVED &&
                        c.getStatus() != CaseStatus.CLOSED)
            .count();
        stats.put("urgentOpen", urgent);

        return stats;
    }

    /**
     * Gets available products from configuration.
     */
    public List<String> getProducts() {
        return config.products();
    }

    /**
     * Gets current account information.
     */
    public AccountInfoDto getCurrentAccount() {
        if (!isConfigured()) {
            return null;
        }
        return casesClient.getCurrentAccount();
    }

    /**
     * Gets the user's entitlements (subscriptions).
     * Entitlements determine which products can be used for case creation.
     */
    public List<EntitlementDto> getEntitlements() {
        if (!isConfigured()) {
            return Collections.emptyList();
        }
        return casesClient.getEntitlements();
    }

    // ========== Mapping methods ==========

    private SupportCase mapToSupportCase(CaseDetailDto detail) {
        SupportCase c = new SupportCase();
        c.setId(detail.getCaseNumber());
        c.setTitle(detail.getSummary());
        c.setDescription(detail.getDescription());
        c.setProduct(detail.getProduct());
        c.setVersion(detail.getVersion());
        c.setReporter(detail.getContactSSOName());

        c.setPriority(mapSeverityToPriority(detail.getSeverity()));
        c.setStatus(mapApiStatusToEnum(detail.getStatus()));

        if (detail.getCreatedDate() != null) {
            c.setCreatedAt(LocalDateTime.ofInstant(detail.getCreatedDate(), ZoneId.systemDefault()));
        }
        if (detail.getLastModifiedDate() != null) {
            c.setUpdatedAt(LocalDateTime.ofInstant(detail.getLastModifiedDate(), ZoneId.systemDefault()));
        }

        if (detail.getComments() != null) {
            List<CaseComment> comments = detail.getComments().stream()
                .map(this::mapToComment)
                .collect(Collectors.toList());
            c.setComments(comments);
        }

        return c;
    }

    private CaseComment mapToComment(CaseCommentDto dto) {
        CaseComment comment = new CaseComment();
        comment.setId(dto.getId());
        comment.setAuthor(dto.getCreatedBy());
        comment.setContent(dto.getCommentBody());
        if (dto.getCreatedDate() != null) {
            comment.setCreatedAt(LocalDateTime.ofInstant(dto.getCreatedDate(), ZoneId.systemDefault()));
        }
        return comment;
    }

    private CasePriority mapSeverityToPriority(String severity) {
        if (severity == null) return CasePriority.NORMAL;
        return switch (severity.toLowerCase()) {
            case "1 (urgent)", "urgent", "1" -> CasePriority.URGENT;
            case "2 (high)", "high", "2" -> CasePriority.HIGH;
            case "3 (normal)", "normal", "3" -> CasePriority.NORMAL;
            case "4 (low)", "low", "4" -> CasePriority.LOW;
            default -> CasePriority.NORMAL;
        };
    }

    private String mapPriorityToSeverity(String priority) {
        if (priority == null) return "3 (Normal)";
        return switch (priority.toUpperCase()) {
            case "URGENT" -> "1 (Urgent)";
            case "HIGH" -> "2 (High)";
            case "NORMAL" -> "3 (Normal)";
            case "LOW" -> "4 (Low)";
            default -> "3 (Normal)";
        };
    }

    private CaseStatus mapApiStatusToEnum(String status) {
        if (status == null) return CaseStatus.NEW;
        String normalized = status.toLowerCase().replace(" ", "_");
        return switch (normalized) {
            case "new", "waiting_on_red_hat" -> CaseStatus.NEW;
            case "in_progress", "working" -> CaseStatus.IN_PROGRESS;
            case "waiting_on_customer", "waiting_for_customer" -> CaseStatus.WAITING_CUSTOMER;
            case "waiting_on_vendor", "waiting_on_engineering" -> CaseStatus.WAITING_VENDOR;
            case "resolved", "solution_proposed" -> CaseStatus.RESOLVED;
            case "closed" -> CaseStatus.CLOSED;
            default -> CaseStatus.IN_PROGRESS;
        };
    }

    private String mapStatusToApi(String status) {
        if (status == null) return "Waiting on Red Hat";
        String normalized = status.toUpperCase().replace(" ", "_").replace("-", "_");
        return switch (normalized) {
            case "NEW", "IN_PROGRESS", "WAITING_ON_RED_HAT", "OPEN", "REOPEN", "REOPENED" -> "Waiting on Red Hat";
            case "WAITING_CUSTOMER", "WAITING_ON_CUSTOMER" -> "Waiting on Customer";
            case "CLOSED", "CLOSE" -> "Closed";
            default -> status; // Pass through for direct API values like "Waiting on Red Hat"
        };
    }
}
