package com.cnpm.bottomcv.service;

/**
 * Service for sending system emails (verification, password reset, etc.)
 */
public interface EmailService {

    /**
     * Send verification email to user
     */
    void sendVerificationEmail(String email, String token);

    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(String email, String token);
}
