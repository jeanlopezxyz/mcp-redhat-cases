package com.redhat.cases.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO para el detalle de un caso de soporte desde la API de Red Hat.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseDetailDto {

    private String caseNumber;
    private String summary;
    private String description;
    private String status;
    private String severity;
    private String product;
    private String version;
    private String caseType;
    private String accountNumber;
    private String contactSSOName;
    private String contactName;
    private String createdById;
    private Instant createdDate;
    private String lastModifiedById;
    private Instant lastModifiedDate;
    private Instant closedDate;
    private Boolean isClosed;
    private String resolutionDescription;
    private String environment;
    private String issue;
    private String hostname;
    private String groupNumber;
    private String groupName;
    private String entitlementSla;
    private String caseLanguage;
    private String alternateId;
    private String openshiftClusterID;
    private String openshiftClusterVersion;
    private Boolean customerEscalation;
    private Boolean fts;
    private Boolean enhancedSLA;
    private List<CaseCommentDto> comments;
    private List<String> notificationAddressEmails;
    private List<String> sbrGroups;

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

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

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getContactSSOName() { return contactSSOName; }
    public void setContactSSOName(String contactSSOName) { this.contactSSOName = contactSSOName; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getCreatedById() { return createdById; }
    public void setCreatedById(String createdById) { this.createdById = createdById; }

    public Instant getCreatedDate() { return createdDate; }
    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }

    public String getLastModifiedById() { return lastModifiedById; }
    public void setLastModifiedById(String lastModifiedById) { this.lastModifiedById = lastModifiedById; }

    public Instant getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(Instant lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    public Instant getClosedDate() { return closedDate; }
    public void setClosedDate(Instant closedDate) { this.closedDate = closedDate; }

    public Boolean getIsClosed() { return isClosed; }
    public void setIsClosed(Boolean isClosed) { this.isClosed = isClosed; }

    public String getResolutionDescription() { return resolutionDescription; }
    public void setResolutionDescription(String resolutionDescription) { this.resolutionDescription = resolutionDescription; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getGroupNumber() { return groupNumber; }
    public void setGroupNumber(String groupNumber) { this.groupNumber = groupNumber; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getEntitlementSla() { return entitlementSla; }
    public void setEntitlementSla(String entitlementSla) { this.entitlementSla = entitlementSla; }

    public String getCaseLanguage() { return caseLanguage; }
    public void setCaseLanguage(String caseLanguage) { this.caseLanguage = caseLanguage; }

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

    public List<CaseCommentDto> getComments() { return comments; }
    public void setComments(List<CaseCommentDto> comments) { this.comments = comments; }

    public List<String> getNotificationAddressEmails() { return notificationAddressEmails; }
    public void setNotificationAddressEmails(List<String> notificationAddressEmails) { this.notificationAddressEmails = notificationAddressEmails; }

    public List<String> getSbrGroups() { return sbrGroups; }
    public void setSbrGroups(List<String> sbrGroups) { this.sbrGroups = sbrGroups; }
}
