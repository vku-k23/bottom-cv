package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.model.VerificationToken;
import com.cnpm.bottomcv.constant.StatusVerificationToken;

public interface VerificationTokenService {
    void saveVerificationToken(VerificationToken verificationToken, long ttMinutes);

    VerificationToken getVerificationToken(String tokenKey);

    VerificationToken getVerificationTokenByEmail(String email);

    void updateStatus(String tokenKey, StatusVerificationToken newStatus);

    void deleteVerificationToken(String tokenKey);
}
