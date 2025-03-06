package com.cnpm.bottomcv.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusJob {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String displayName;

    StatusJob(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
