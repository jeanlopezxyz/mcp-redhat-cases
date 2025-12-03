package com.redhat.cases.domain.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Domain entity representing a comment on a support case.
 */
public class CaseComment {

    private String id;
    private String author;
    private String content;
    private LocalDateTime createdAt;
    private boolean isInternal;

    public CaseComment() {
        this.createdAt = LocalDateTime.now();
        this.isInternal = false;
    }

    public CaseComment(String id, String author, String content) {
        this();
        this.id = id;
        this.author = author;
        this.content = content;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isInternal() { return isInternal; }
    public void setInternal(boolean internal) { isInternal = internal; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String visibility = isInternal ? "[INTERNAL] " : "";
        return String.format("%s[%s] %s:\n  %s",
            visibility, createdAt.format(formatter), author, content);
    }
}
