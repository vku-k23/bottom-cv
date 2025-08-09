package com.cnpm.bottomcv.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    PENDING("PENDING");

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }
}
