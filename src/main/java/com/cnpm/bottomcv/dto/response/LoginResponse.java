package com.cnpm.bottomcv.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
@ToString
public class LoginResponse {
    private String token;
    private long expiresIn;
}