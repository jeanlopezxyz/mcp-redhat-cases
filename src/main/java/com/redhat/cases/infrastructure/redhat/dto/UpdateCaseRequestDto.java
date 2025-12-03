package com.redhat.cases.infrastructure.redhat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO para actualizar un caso existente en la API de Red Hat.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateCaseRequestDto {

    private String summary;
    private String description;
    private String status;
    private String severity;
    private String product;
    private String version;
    private String caseType;
    private String environment;
    private String issue;
    private String hostname;
    private String groupNumber;
    private String caseLanguage;
    private String contactSSOName;
    private String alternateId;
    private String openshiftClusterID;
    private String openshiftClusterVersion;
    private Boolean customerEscalation;
    private Boolean fts;
    private Boolean enhancedSLA;

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getCaseType() { return caseType; }
    public void setCaseType(String caseType) { this.caseType = caseType; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getGroupNumber() { return groupNumber; }
    public void setGroupNumber(String groupNumber) { this.groupNumber = groupNumber; }

    public String getCaseLanguage() { return caseLanguage; }
    public void setCaseLanguage(String caseLanguage) { this.caseLanguage = caseLanguage; }

    public String getContactSSOName() { return contactSSOName; }
    public void setContactSSOName(String contactSSOName) { this.contactSSOName = contactSSOName; }

    public String getAlternateId() { return alternateId; }
    public void setAlternateId(String alternateId) { this.alternateId = alternateId; }

    public String getOpenshiftClusterID() { return openshiftClusterID; }
    public void setOpenshiftClusterID(String openshiftClusterID) { this.openshiftClusterID = openshiftClusterID; }

    public String getOpenshiftClusterVersion() { return openshiftClusterVersion; }
    public void setOpenshiftClusterVersion(String openshiftClusterVersion) { this.openshiftClusterVersion = openshiftClusterVersion; }

    public Boolean getCustomerEscalation() { return customerEscalation; }
    public void setCustomerEscalation(Boolean customerEscalation) { this.customerEscalation = customerEscalation; }

    public Boolean getFts() { return fts; }
    public void setFts(Boolean fts) { this.fts = fts; }

    public Boolean getEnhancedSLA() { return enhancedSLA; }
    public void setEnhancedSLA(Boolean enhancedSLA) { this.enhancedSLA = enhancedSLA; }
}
