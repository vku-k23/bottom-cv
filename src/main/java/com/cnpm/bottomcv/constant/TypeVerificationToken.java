package com.cnpm.bottomcv.constant;

import lombok.Getter;

@Getter
public enum TypeVerificationToken {
    EMAIL("email"),
    FORGOT_PASSWORD("forgot_password"),
    REGISTER("register");

    private final String type;

    TypeVerificationToken(String type) {
        this.type = type;
    }

    public enum StatusOTP {
        READY_RESEND("ready_resend"),
        EXPIRED("expired"),
        USED("used"),
        NOT_FOUND("not_found");

        private final String type;

        StatusOTP(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

}
