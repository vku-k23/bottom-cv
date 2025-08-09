package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.LoginUserDto;
import com.cnpm.bottomcv.dto.RegisterUserDto;
import com.cnpm.bottomcv.model.User;

public interface AuthenticationService {

        User signup(RegisterUserDto registerUserDto);

        User authenticate(LoginUserDto input);

        void forgotPassword(String request);

        void resetPassword(String token, String newPassword);

        void sendVerificationEmail(String email);

        void sendVerificationPhoneNumber(String phoneNumber);

        void confirmVerificationEmail(String token);

        void confirmForgotPassword(String token);

        void logout(String refreshToken);
}