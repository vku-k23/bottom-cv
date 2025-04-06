package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.NotificationRequest;
import com.cnpm.bottomcv.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    NotificationResponse getNotificationById(Long id);

    List<NotificationResponse> getNotificationsByUserId(Long userId);

    NotificationResponse markAsRead(Long id);

    void deleteNotification(Long id);
}