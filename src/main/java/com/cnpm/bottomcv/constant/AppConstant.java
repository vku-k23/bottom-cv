package com.cnpm.bottomcv.constant;

/**
 * Application-wide constants to avoid code duplication.
 * All string literals used 3+ times should be defined here.
 */
public final class AppConstant {

    private AppConstant() {
        // Utility class - prevent instantiation
    }

    // Response JSON keys
    public static final String RESPONSE_KEY_SUCCESS = "success";
    public static final String RESPONSE_KEY_MESSAGE = "message";
    public static final String RESPONSE_KEY_ERROR = "error";
    public static final String RESPONSE_KEY_STATUS = "status";
    public static final String RESPONSE_KEY_TIMESTAMP = "timestamp";
    public static final String RESPONSE_KEY_DESCRIPTION = "description";
    public static final String RESPONSE_KEY_FILES = "files";
    public static final String RESPONSE_KEY_COUNT = "count";

    // Field names for error messages
    public static final String FIELD_APPLY_ID = "applyId";
    public static final String FIELD_APPLY_ID_LABEL = "Apply id";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_USERNAME_LABEL = "Username";
    public static final String FIELD_CV_ID = "CV id";
    public static final String FIELD_COMPANY_ID = "companyId";
    public static final String FIELD_COMPANY_ID_LABEL = "Company id";
    public static final String FIELD_JOB_ID = "Job id";
    public static final String FIELD_NOTIFICATION_ID = "Notification id";
    public static final String FIELD_REVIEW_ID = "Review id";
    public static final String FIELD_PROFILE_ID = "Profile id";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_PROFILE = "profile";
    public static final String FIELD_CONTENT = "content";

    // Entity names
    public static final String ENTITY_APPLICATION = "Application";
    public static final String ENTITY_STATUS_COLUMN = "StatusColumn";

    // Role names
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_EMPLOYER = "EMPLOYER";

    // Other constants
    public static final String ADMIN_USERNAME = "admin";
}
