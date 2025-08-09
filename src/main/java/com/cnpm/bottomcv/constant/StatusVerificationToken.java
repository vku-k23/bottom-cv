package com.cnpm.bottomcv.constant;

import lombok.Getter;

@Getter
public enum StatusVerificationToken {
    WAITING("waiting"),
    IN_PROGRESS("in_progress"),
    DONE("done");

    private final String status;

    StatusVerificationToken(String status) {
        this.status = status;
    }

}
