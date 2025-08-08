package com.cnpm.bottomcv.service;

public interface AuthExtraService {
    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    void sendVerificationEmail(String email);

    void confirmVerificationEmail(String token);

    void logout();
}