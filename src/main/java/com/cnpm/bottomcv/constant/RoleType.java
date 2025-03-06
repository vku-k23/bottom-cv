package com.cnpm.bottomcv.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleType {
    ADMIN("ADMIN"),
    EMPLOYER("EMPLOYER"),
    USER("USER"),
    ;
    private final String displayName;

    RoleType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}