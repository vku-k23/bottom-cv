package com.cnpm.bottomcv.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SubscriptionStatus {
    ACTIVE("ACTIVE"),
    EXPIRED("EXPIRED"),
    CANCELLED("CANCELLED"),
    FAILED("FAILED");

    private final String displayName;

    SubscriptionStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}

