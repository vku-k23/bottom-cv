package com.cnpm.bottomcv.constant;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EmailTemplateType {
    INTERVIEW("INTERVIEW"),
    OFFER("OFFER"),
    REJECTION("REJECTION"),
    CUSTOM("CUSTOM");

    private final String displayName;

    EmailTemplateType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
