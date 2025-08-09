package com.cnpm.bottomcv.constant;

public final class TimeFormat {
    public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_TIME_FORMAT_WITH_ZONE = "dd-MM-yyyy HH:mm:ss Z";
    public static final String DATE_TIME_FORMAT_SLASH = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_FORMAT_SLASH = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT_SLASH_WITH_ZONE = "dd/MM/yyyy HH:mm:ss Z";

    public static String getDisplayName(String format) {
        return switch (format) {
            case DATE_TIME_FORMAT -> DATE_TIME_FORMAT;
            case DATE_FORMAT -> DATE_FORMAT;
            case TIME_FORMAT -> TIME_FORMAT;
            case DATE_TIME_FORMAT_WITH_ZONE -> DATE_TIME_FORMAT_WITH_ZONE;
            case DATE_TIME_FORMAT_SLASH -> DATE_TIME_FORMAT_SLASH;
            case DATE_FORMAT_SLASH -> DATE_FORMAT_SLASH;
            case DATE_TIME_FORMAT_SLASH_WITH_ZONE -> DATE_TIME_FORMAT_SLASH_WITH_ZONE;
            default -> throw new IllegalArgumentException("Invalid format: " + format);
        };
    }
}
