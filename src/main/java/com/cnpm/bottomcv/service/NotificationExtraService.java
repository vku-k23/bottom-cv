package com.cnpm.bottomcv.service;

public interface NotificationExtraService {
    void listNotifications(String status);

    void markAllAsRead();

    void unreadCount();

    void updatePreferences();
}