package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.NotificationRequest;
import com.cnpm.bottomcv.dto.response.NotificationResponse;
import com.cnpm.bottomcv.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification API", description = "The API of notification")
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Back APIs (for dashboard - EMPLOYER, ADMIN)
    @PostMapping("/back/notifications")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/back/notifications/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<NotificationResponse> getNotificationByIdForBack(@PathVariable Long id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/back/notifications/user/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserIdForBack(@PathVariable Long userId) {
        List<NotificationResponse> responses = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/back/notifications/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    // Front APIs (for client web - CANDIDATE, EMPLOYER)
    @GetMapping("/front/notifications/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EMPLOYER')")
    public ResponseEntity<NotificationResponse> getNotificationByIdForFront(@PathVariable Long id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/front/notifications/user/{userId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EMPLOYER')")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserIdForFront(@PathVariable Long userId) {
        List<NotificationResponse> responses = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/front/notifications/{id}/read")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'EMPLOYER')")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(response);
    }
}