package com.cnpm.bottomcv.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SubscriptionPlanType {
    BASIC("BASIC"),
    STANDARD("STANDARD"),
    PREMIUM("PREMIUM");

    private final String displayName;

    SubscriptionPlanType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}

