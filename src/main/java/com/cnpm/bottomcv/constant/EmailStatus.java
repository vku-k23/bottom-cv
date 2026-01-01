package com.cnpm.bottomcv.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EmailStatus {
    PENDING("PENDING"),
    SENT("SENT"),
    FAILED("FAILED"),
    DELIVERED("DELIVERED");

    private final String displayName;

    EmailStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
