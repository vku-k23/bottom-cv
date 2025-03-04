package com.cnpm.bottomcv.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}