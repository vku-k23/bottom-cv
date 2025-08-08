package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.service.AuthExtraService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication Extra API", description = "Skeleton endpoints for password reset, email verification, and logout")
@RestController
@RequestMapping(value = "/api/v1/auth", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class AuthExtraController {

    private final AuthExtraService authExtraService;

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        authExtraService.forgotPassword(email);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authExtraService.resetPassword(token, newPassword);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/verify-email/send")
    public ResponseEntity<Void> sendVerificationEmail(@RequestParam String email) {
        authExtraService.sendVerificationEmail(email);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/verify-email/confirm")
    public ResponseEntity<Void> confirmVerificationEmail(@RequestParam String token) {
        authExtraService.confirmVerificationEmail(token);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authExtraService.logout();
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}