package com.redhat.cases.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a Red Hat support case.
 */
public class SupportCase {

    private String id;
    private String title;
    private String description;
    private CaseStatus status;
    private CasePriority priority;
    private String product;
    private String version;
    private String component;
    private String assignee;
    private String reporter;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CaseComment> comments;

    public SupportCase() {
        this.comments = new ArrayList<>();
        this.status = CaseStatus.NEW;
        this.priority = CasePriority.NORMAL;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public SupportCase(String id, String title, String description) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CaseStatus getStatus() { return status; }
    public void setStatus(CaseStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public CasePriority getPriority() { return priority; }
    public void setPriority(CasePriority priority) { this.priority = priority; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }

    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<CaseComment> getComments() { return comments; }
    public void setComments(List<CaseComment> comments) { this.comments = comments; }

    public void addComment(CaseComment comment) {
        this.comments.add(comment);
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%s) - %s",
            id, title, status.getDisplayName(), priority.getDisplayName(), product);
    }

    /**
     * Generates the support portal URL for this case.
     */
    public String getCaseUrl() {
        return "https://access.redhat.com/support/cases/#/case/" + id;
    }

    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Case: ").append(id).append(" ===\n");
        sb.append("URL: ").append(getCaseUrl()).append("\n");
        sb.append("Title: ").append(title).append("\n");
        sb.append("Status: ").append(status.getDisplayName()).append("\n");
        sb.append("Priority: ").append(priority.getDisplayName()).append("\n");
        sb.append("Product: ").append(product != null ? product : "N/A");
        if (version != null) sb.append(" ").append(version);
        sb.append("\n");
        if (component != null) sb.append("Component: ").append(component).append("\n");
        sb.append("Reported by: ").append(reporter != null ? reporter : "N/A").append("\n");
        sb.append("Assigned to: ").append(assignee != null ? assignee : "Unassigned").append("\n");
        sb.append("Created: ").append(createdAt).append("\n");
        sb.append("Updated: ").append(updatedAt).append("\n");
        sb.append("\nDescription:\n").append(description).append("\n");

        if (!comments.isEmpty()) {
            sb.append("\n--- Comments (").append(comments.size()).append(") ---\n");
            for (CaseComment comment : comments) {
                sb.append(comment.toString()).append("\n");
            }
        }

        return sb.toString();
    }
}
