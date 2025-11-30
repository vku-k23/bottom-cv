package com.cnpm.bottomcv.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    PENDING("PENDING"),
    BANNED("BANNED");

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }
}
