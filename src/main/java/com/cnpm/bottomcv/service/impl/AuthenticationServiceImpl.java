package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.AppConstant;

import com.cnpm.bottomcv.constant.*;
import com.cnpm.bottomcv.dto.LoginUserDto;
import com.cnpm.bottomcv.dto.RegisterUserDto;
import com.cnpm.bottomcv.exception.BadRequestException;
import com.cnpm.bottomcv.exception.ResourceAlreadyExistException;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Profile;
import com.cnpm.bottomcv.model.Role;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.model.VerificationToken;
import com.cnpm.bottomcv.repository.ProfileRepository;
import com.cnpm.bottomcv.repository.RoleRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.AuthenticationService;
import com.cnpm.bottomcv.service.EmailService;
import com.cnpm.bottomcv.service.RefreshTokenService;
import com.cnpm.bottomcv.service.VerificationTokenService;
import com.cnpm.bottomcv.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;

    @Override
    public User signup(RegisterUserDto registerUserDto) {

        if (userRepository.existsByUsername(registerUserDto.getUsername())) {
            throw new ResourceAlreadyExistException("Username already exists");
        }

        if (profileRepository.existsByEmail(registerUserDto.getEmail())) {
            throw new ResourceAlreadyExistException("Email already exists");
        }

        if (profileRepository.existsByPhoneNumber(registerUserDto.getPhoneNumber())) {
            throw new ResourceAlreadyExistException("Phone number already exists");
        }

        User user = User.builder()
                .username(registerUserDto.getUsername())
                .userCode(UUID.randomUUID().toString())
                .status(UserStatus.PENDING)
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .build();

        Set<Role> roles = new HashSet<>(Collections.singleton(roleRepository.findByName(RoleType.CANDIDATE)
                .orElseThrow(() -> new RuntimeException(
                        "Role not found: " + RoleType.CANDIDATE.name()))));
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        Profile profile = Profile.builder()
                .firstName(registerUserDto.getFirstName())
                .lastName(registerUserDto.getLastName())
                .email(registerUserDto.getEmail())
                .phoneNumber(registerUserDto.getPhoneNumber())
                .dayOfBirth(registerUserDto.getDayOfBirth())
                .user(savedUser)
                .build();

        profileRepository.save(profile);

        sendVerificationEmail(registerUserDto.getEmail());

        return savedUser;
    }

    @Override
    public User authenticate(LoginUserDto input) {

        User user = userRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(AppConstant.FIELD_USERNAME_LABEL, AppConstant.FIELD_USERNAME,
                        input.getUsername()));
        if (user.getStatus() == UserStatus.PENDING) {
            throw new BadRequestException("Please verify your email to login.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()));

        return user;
    }

    @Override
    public void forgotPassword(String request) {
        Pattern emailPattern = Pattern.compile(PatternField.EMAIL_PATTERN);
        Pattern phonePattern = Pattern.compile(PatternField.PHONE_NUMBER_PATTERN);

        if (emailPattern.matcher(request).matches()) {
            // Send url to mail and verify

            if (!profileRepository.existsByEmail(request)) {
                throw new ResourceNotFoundException("Email", "email", request);
            }

            if (!userRepository.isUserActiveWithEmail(request)) {
                throw new BadRequestException("Email is not active. Please verify your email first.");
            }

            VerificationToken alreadyVT = verificationTokenService.getVerificationTokenByEmail(request);

            if (alreadyVT != null &&
                    alreadyVT.getType() == TypeVerificationToken.FORGOT_PASSWORD &&
                    (alreadyVT.getStatus() == StatusVerificationToken.WAITING ||
                            alreadyVT.getStatus() == StatusVerificationToken.IN_PROGRESS)) {
                return;
            }

            String token = buildResetPasswordToken(request);
            VerificationToken verificationToken = VerificationToken.builder()
                    .token(token)
                    .type(TypeVerificationToken.FORGOT_PASSWORD)
                    .status(StatusVerificationToken.WAITING)
                    .email(request)
                    .build();
            verificationTokenService.saveVerificationToken(verificationToken, 15);
            emailService.sendPasswordResetEmail(request, token);
        } else if (phonePattern.matcher(request).matches()) {
            // Send otp to phone number and verify
            sendVerificationPhoneNumber(request);
        } else {
            throw new IllegalArgumentException(
                    "Invalid request format. Please provide a valid email or phone number.");
        }
    }

    @Override
    public void confirmForgotPassword(String token) {
        try {
            VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
            if (verificationToken == null
                    || verificationToken.getType() != TypeVerificationToken.FORGOT_PASSWORD) {
                throw new BadRequestException("Invalid forgot password token.");
            }
            if (verificationToken.getStatus() != StatusVerificationToken.WAITING) {
                throw new IllegalArgumentException("Forgot password token is not in waiting status.");
            }
            jwtService.extractUsernameIgnoreExpiration(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid forgot password token.");
        }

        if (jwtService.isTokenExpired(token)) {
            throw new IllegalArgumentException("Forgot password token has expired.");
        }
        verificationTokenService.updateStatus(token, StatusVerificationToken.IN_PROGRESS);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        Pattern passwordPolicy = Pattern
                .compile(PatternField.PASSWORD_PATTERN);
        if (newPassword == null || !passwordPolicy.matcher(newPassword).matches()) {
            throw new IllegalArgumentException("Password does not meet security requirements.");
        }

        final String username;
        VerificationToken verificationToken;
        try {
            verificationToken = verificationTokenService.getVerificationToken(token);
            if (verificationToken == null
                    || verificationToken.getType() != TypeVerificationToken.FORGOT_PASSWORD) {
                throw new IllegalArgumentException("Invalid reset token.");
            }
            if (verificationToken.getStatus() != StatusVerificationToken.IN_PROGRESS) {
                throw new IllegalArgumentException("Reset token is not in progress status.");
            }
            username = jwtService.extractUsernameIgnoreExpiration(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid reset token.");
        }

        if (jwtService.isTokenExpired(token)) {
            throw new IllegalArgumentException("Reset token has expired.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.setPassword(passwordEncoder.encode(newPassword));

        verificationTokenService.deleteVerificationToken(token);

        userRepository.save(user);

        refreshTokenService.deleteByUserId(user.getId());
    }

    @Override
    public void sendVerificationEmail(String email) {
        if (email == null || !Pattern.compile(PatternField.EMAIL_PATTERN).matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        if (!userRepository.existsByEmail(email)) {
            throw new ResourceNotFoundException("Email", "email", email);
        }

        if (userRepository.isUserActiveWithEmail(email)) {
            throw new BadRequestException("Email is already verified.");
        }

        VerificationToken verificationToken = verificationTokenService.getVerificationTokenByEmail(email);

        if (verificationToken != null && verificationToken.getType() == TypeVerificationToken.EMAIL) {
            if (verificationToken.getStatus() == StatusVerificationToken.WAITING) {
                return;
            }
        }

        String token = buildVerificationEmailToken(email);
        verificationTokenService.saveVerificationToken(
                VerificationToken.builder()
                        .token(token)
                        .type(TypeVerificationToken.EMAIL)
                        .status(StatusVerificationToken.WAITING)
                        .email(email)
                        .build(),
                15);

        emailService.sendVerificationEmail(email, token);
    }

    @Override
    public void sendVerificationPhoneNumber(String phoneNumber) {
        // After verify otp code,
    }

    @Override
    public void confirmVerificationEmail(String token) {
        final String username;
        try {
            VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
            if (verificationToken == null || verificationToken.getType() != TypeVerificationToken.EMAIL) {
                throw new IllegalArgumentException("Invalid verification token.");
            }
            if (verificationToken.getStatus() != StatusVerificationToken.WAITING) {
                throw new IllegalArgumentException("Verification token is not in waiting status.");
            }
            username = jwtService.extractUsernameIgnoreExpiration(token);
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            throw new IllegalArgumentException("Invalid verification token: " + e.getMessage());
        }

        if (jwtService.isTokenExpired(token)) {
            throw new IllegalArgumentException("Verification token has expired.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setStatus(UserStatus.ACTIVE);

        verificationTokenService.updateStatus(token, StatusVerificationToken.DONE);

        userRepository.save(user);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
    }

    private String buildVerificationEmailToken(String email) {
        String username = profileRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email", "email", email))
                .getUser()
                .getUsername();
        return jwtService.generateToken(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstant.FIELD_USERNAME_LABEL, AppConstant.FIELD_USERNAME,
                        username)));
    }

    private String buildResetPasswordToken(String email) {
        String username = profileRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email", "email", email))
                .getUser()
                .getUsername();
        return jwtService.generateToken(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstant.FIELD_USERNAME_LABEL, AppConstant.FIELD_USERNAME,
                        username)));
    }
}