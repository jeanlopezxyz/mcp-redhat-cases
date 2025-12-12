package com.redhat.cases.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for Red Hat Support Entitlements.
 * Represents a product subscription that allows case creation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntitlementDto {

    private String id;
    private String name;
    private String startDate;
    private String endDate;
    private String serviceLevel;
    private String supportLevel;
    private String slaProcessId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getServiceLevel() { return serviceLevel; }
    public void setServiceLevel(String serviceLevel) { this.serviceLevel = serviceLevel; }

    public String getSupportLevel() { return supportLevel; }
    public void setSupportLevel(String supportLevel) { this.supportLevel = supportLevel; }

    public String getSlaProcessId() { return slaProcessId; }
    public void setSlaProcessId(String slaProcessId) { this.slaProcessId = slaProcessId; }

    /**
     * Check if this entitlement allows creating support cases.
     * Self-supported entitlements typically don't allow case creation.
     */
    public boolean allowsCaseCreation() {
        return supportLevel != null && !supportLevel.equalsIgnoreCase("SELF-SUPPORTED");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("- ").append(name);
        if (supportLevel != null) {
            sb.append(" [").append(supportLevel).append("]");
        }
        if (endDate != null) {
            sb.append(" (expires: ").append(endDate.replace("Z", "")).append(")");
        }
        return sb.toString();
    }
}
