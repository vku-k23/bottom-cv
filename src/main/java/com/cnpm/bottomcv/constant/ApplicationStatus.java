package com.cnpm.bottomcv.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicationStatus {
    NEW("NEW"),
    SCREENING("SCREENING"),
    INTERVIEW("INTERVIEW"),
    OFFER("OFFER"),
    HIRED("HIRED"),
    REJECTED("REJECTED"),
    WITHDRAWN("WITHDRAWN");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}

