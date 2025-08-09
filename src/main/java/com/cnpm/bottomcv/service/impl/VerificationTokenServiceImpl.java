package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.StatusVerificationToken;
import com.cnpm.bottomcv.model.VerificationToken;
import com.cnpm.bottomcv.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveVerificationToken(VerificationToken verificationToken, long ttMinutes) {
        String tokenKey = verificationToken.getToken();
        redisTemplate.opsForValue().set(tokenKey, verificationToken, ttMinutes, TimeUnit.MINUTES);
        redisTemplate.expire(tokenKey, ttMinutes, TimeUnit.MINUTES);
    }

    @Override
    public VerificationToken getVerificationToken(String tokenKey) {
        return (VerificationToken) redisTemplate.opsForValue().get(tokenKey);
    }

    @Override
    public VerificationToken getVerificationTokenByEmail(String email) {
        return redisTemplate.keys("*")
                .stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(VerificationToken.class::isInstance)
                .map(VerificationToken.class::cast)
                .filter(token -> Objects.equals(token.getEmail(), email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateStatus(String tokenKey, StatusVerificationToken newStatus) {
        VerificationToken token = getVerificationToken(tokenKey);
        if (token != null) {
            token.setStatus(newStatus);
            redisTemplate.opsForValue().set(tokenKey, token);
        }
    }

    @Override
    public void deleteVerificationToken(String tokenKey) {
        if (redisTemplate.hasKey(tokenKey)) {
            redisTemplate.delete(tokenKey);
        }
    }
}
