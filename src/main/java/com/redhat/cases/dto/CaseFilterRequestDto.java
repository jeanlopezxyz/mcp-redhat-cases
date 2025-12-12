package com.redhat.cases.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO para filtrar casos de soporte en la API de Red Hat.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseFilterRequestDto {

    private Integer maxResults = 50;
    private Integer offset;
    private Boolean includeClosed;
    private String status;
    private String severity;
    private String product;
    private String keyword;
    private String accountNumber;
    private String sortField;
    private String sortOrder;
    private List<String> statuses;
    private List<String> severities;
    private List<String> products;
    private List<String> caseNumbers;

    public CaseFilterRequestDto() {}

    public CaseFilterRequestDto(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Integer getMaxResults() { return maxResults; }
    public void setMaxResults(Integer maxResults) { this.maxResults = maxResults; }

    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }

    public Boolean getIncludeClosed() { return includeClosed; }
    public void setIncludeClosed(Boolean includeClosed) { this.includeClosed = includeClosed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getSortField() { return sortField; }
    public void setSortField(String sortField) { this.sortField = sortField; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public List<String> getStatuses() { return statuses; }
    public void setStatuses(List<String> statuses) { this.statuses = statuses; }

    public List<String> getSeverities() { return severities; }
    public void setSeverities(List<String> severities) { this.severities = severities; }

    public List<String> getProducts() { return products; }
    public void setProducts(List<String> products) { this.products = products; }

    public List<String> getCaseNumbers() { return caseNumbers; }
    public void setCaseNumbers(List<String> caseNumbers) { this.caseNumbers = caseNumbers; }
}
