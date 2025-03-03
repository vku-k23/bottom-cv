package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.LoginUserDto;
import com.cnpm.bottomcv.dto.RegisterUserDto;
import com.cnpm.bottomcv.dto.request.RefreshTokenRequest;
import com.cnpm.bottomcv.dto.response.LoginResponse;
import com.cnpm.bottomcv.dto.response.RefreshTokenResponse;
import com.cnpm.bottomcv.model.RefreshToken;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.service.AuthenticationService;
import com.cnpm.bottomcv.service.RefreshTokenService;
import com.cnpm.bottomcv.service.jwt.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication API", description = "The API of JWT authentication")
@RequestMapping(value = "/api/auth", produces = {MediaType.APPLICATION_JSON_VALUE})
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authenticatedUser.getId());
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(loginResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        String requestRefreshToken = refreshTokenRequest.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user);
                    return ResponseEntity.ok(new RefreshTokenResponse(accessToken, requestRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }
}