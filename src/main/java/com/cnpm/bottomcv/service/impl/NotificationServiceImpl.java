package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.dto.request.NotificationRequest;
import com.cnpm.bottomcv.dto.response.NotificationResponse;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Notification;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.NotificationRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        mapRequestToEntity(notification, request);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setCreatedBy("system");
        notification.setRead(false);
        notificationRepository.save(notification);
        return mapToResponse(notification);
    }

    @Override
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification id", "id", id.toString()));
        return mapToResponse(notification);
    }

    @Override
    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification id", "id", id.toString()));
        notification.setRead(true);
        notification.setUpdatedAt(LocalDateTime.now());
        notification.setUpdatedBy("system");
        notificationRepository.save(notification);
        return mapToResponse(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification id", "id", id.toString()));
        notificationRepository.delete(notification);
    }

    private void mapRequestToEntity(Notification notification, NotificationRequest request) {
        notification.setMessage(request.getMessage());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User id", "userId", request.getUserId().toString()));
        notification.setUser(user);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setMessage(notification.getMessage());
        response.setRead(notification.isRead());
        response.setUserId(notification.getUser().getId());
        response.setCreatedAt(notification.getCreatedAt());
        response.setCreatedBy(notification.getCreatedBy());
        response.setUpdatedAt(notification.getUpdatedAt());
        response.setUpdatedBy(notification.getUpdatedBy());
        return response;
    }
}