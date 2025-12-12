package com.redhat.cases.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO para la respuesta de listado de casos desde la API de Red Hat.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseListResponseDto {

    private Integer totalCount;
    private List<CaseDetailDto> cases;

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public List<CaseDetailDto> getCases() { return cases; }
    public void setCases(List<CaseDetailDto> cases) { this.cases = cases; }
}
