package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.service.NotificationExtraService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification Extra API", description = "Skeleton endpoints for notification preferences and utilities")
@RestController
@RequestMapping(value = "/api/v1/front/notifications", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class NotificationExtraController {

    private final NotificationExtraService notificationExtraService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER')")
    public ResponseEntity<Void> listNotifications(@RequestParam(required = false, defaultValue = "ALL") String status) {
        notificationExtraService.listNotifications(status);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PutMapping("/mark-all-read")
    @PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER')")
    public ResponseEntity<Void> markAllAsRead() {
        notificationExtraService.markAllAsRead();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER')")
    public ResponseEntity<Void> unreadCount() {
        notificationExtraService.unreadCount();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PutMapping("/preferences")
    @PreAuthorize("hasAnyRole('CANDIDATE','EMPLOYER')")
    public ResponseEntity<Void> updatePreferences() {
        notificationExtraService.updatePreferences();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}