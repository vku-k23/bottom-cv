package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.service.AuthExtraService;
import org.springframework.stereotype.Service;

@Service
public class AuthExtraServiceImpl implements AuthExtraService {
    @Override
    public void forgotPassword(String email) {
        // TODO: implement forgot password flow
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        // TODO: implement reset password flow
    }

    @Override
    public void sendVerificationEmail(String email) {
        // TODO: implement sending verification email
    }

    @Override
    public void confirmVerificationEmail(String token) {
        // TODO: implement confirm verification email
    }

    @Override
    public void logout() {
        // TODO: implement logout and token revocation
    }
}