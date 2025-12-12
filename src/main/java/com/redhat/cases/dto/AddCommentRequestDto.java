package com.redhat.cases.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO para agregar un comentario a un caso en la API de Red Hat.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddCommentRequestDto {

    private String commentBody;
    private Boolean doNotChangeStatus;
    private Boolean isDraft;

    public AddCommentRequestDto() {}

    public AddCommentRequestDto(String commentBody) {
        this.commentBody = commentBody;
    }

    public String getCommentBody() { return commentBody; }
    public void setCommentBody(String commentBody) { this.commentBody = commentBody; }

    public Boolean getDoNotChangeStatus() { return doNotChangeStatus; }
    public void setDoNotChangeStatus(Boolean doNotChangeStatus) { this.doNotChangeStatus = doNotChangeStatus; }

    public Boolean getIsDraft() { return isDraft; }
    public void setIsDraft(Boolean isDraft) { this.isDraft = isDraft; }
}
