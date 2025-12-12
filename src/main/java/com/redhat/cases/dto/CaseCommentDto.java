package com.redhat.cases.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO para un comentario de caso desde la API de Red Hat.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseCommentDto {

    private String id;
    private String caseNumber;
    private String commentBody;
    private String createdBy;
    private String createdByType;
    private Instant createdDate;
    private Instant lastModifiedDate;
    private String lastModifiedById;
    private Instant publishedDate;
    private String contentType;
    private Boolean isDraft;
    private Boolean doNotChangeStatus;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getCommentBody() { return commentBody; }
    public void setCommentBody(String commentBody) { this.commentBody = commentBody; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCreatedByType() { return createdByType; }
    public void setCreatedByType(String createdByType) { this.createdByType = createdByType; }

    public Instant getCreatedDate() { return createdDate; }
    public void setCreatedDate(Instant createdDate) { this.createdDate = createdDate; }

    public Instant getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(Instant lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    public String getLastModifiedById() { return lastModifiedById; }
    public void setLastModifiedById(String lastModifiedById) { this.lastModifiedById = lastModifiedById; }

    public Instant getPublishedDate() { return publishedDate; }
    public void setPublishedDate(Instant publishedDate) { this.publishedDate = publishedDate; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Boolean getIsDraft() { return isDraft; }
    public void setIsDraft(Boolean isDraft) { this.isDraft = isDraft; }

    public Boolean getDoNotChangeStatus() { return doNotChangeStatus; }
    public void setDoNotChangeStatus(Boolean doNotChangeStatus) { this.doNotChangeStatus = doNotChangeStatus; }
}
