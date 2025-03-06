package com.cnpm.bottomcv.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusApplication {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    ;

    private final String displayName;

    StatusApplication(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
