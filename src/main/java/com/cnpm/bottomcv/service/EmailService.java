package com.cnpm.bottomcv.service;

public interface EmailService {
    /**
     * Sends a verification email to the user.
     *
     * @param email the email address to send the verification to
     * @param token the verification token
     */
    void sendVerificationEmail(String email, String token);

    /**
     * Sends a password reset email to the user.
     *
     * @param email the email address to send the reset link to
     * @param token the password reset token
     */
    void sendPasswordResetEmail(String email, String token);
}
