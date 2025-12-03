package com.redhat.cases.infrastructure.redhat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO para crear un nuevo caso de soporte en la API de Red Hat.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateCaseRequestDto {

    private String summary;
    private String description;
    private String product;
    private String version;
    private String severity;
    private String caseType;
    private String contactSSOName;
    private String accountNumberRef;
    private String environment;
    private String hostname;
    private String groupNumber;
    private String caseLanguage;
    private String openshiftClusterID;

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getCaseType() { return caseType; }
    public void setCaseType(String caseType) { this.caseType = caseType; }

    public String getContactSSOName() { return contactSSOName; }
    public void setContactSSOName(String contactSSOName) { this.contactSSOName = contactSSOName; }

    public String getAccountNumberRef() { return accountNumberRef; }
    public void setAccountNumberRef(String accountNumberRef) { this.accountNumberRef = accountNumberRef; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getGroupNumber() { return groupNumber; }
    public void setGroupNumber(String groupNumber) { this.groupNumber = groupNumber; }

    public String getCaseLanguage() { return caseLanguage; }
    public void setCaseLanguage(String caseLanguage) { this.caseLanguage = caseLanguage; }

    public String getOpenshiftClusterID() { return openshiftClusterID; }
    public void setOpenshiftClusterID(String openshiftClusterID) { this.openshiftClusterID = openshiftClusterID; }
}
