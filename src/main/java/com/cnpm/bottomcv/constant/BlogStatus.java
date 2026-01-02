package com.cnpm.bottomcv.constant;

import lombok.Getter;

@Getter
public enum BlogStatus {
    DRAFT("Draft"),
    PUBLISHED("Published");

    private final String displayName;

    BlogStatus(String displayName) {
        this.displayName = displayName;
    }
}


