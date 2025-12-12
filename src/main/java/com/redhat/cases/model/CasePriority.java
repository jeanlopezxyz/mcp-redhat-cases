package com.redhat.cases.model;

/**
 * Priorities of a Red Hat support case.
 */
public enum CasePriority {
    LOW("Low", 4),
    NORMAL("Normal", 3),
    HIGH("High", 2),
    URGENT("Urgent", 1);

    private final String displayName;
    private final int level;

    CasePriority(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }
}
