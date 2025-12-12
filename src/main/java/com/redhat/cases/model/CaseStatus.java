package com.redhat.cases.model;

/**
 * Possible states of a Red Hat support case.
 */
public enum CaseStatus {
    NEW("New"),
    IN_PROGRESS("In Progress"),
    WAITING_CUSTOMER("Waiting on Customer"),
    WAITING_VENDOR("Waiting on Vendor"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String displayName;

    CaseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
