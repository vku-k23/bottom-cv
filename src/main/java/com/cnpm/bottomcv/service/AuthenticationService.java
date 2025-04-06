package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.dto.LoginUserDto;
import com.cnpm.bottomcv.dto.RegisterUserDto;
import com.cnpm.bottomcv.model.Profile;
import com.cnpm.bottomcv.model.Role;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.ProfileRepository;
import com.cnpm.bottomcv.repository.RoleRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public User signup(RegisterUserDto registerUserDto) {
        User user = User.builder()
                .username(registerUserDto.getUsername())
                .userCode(UUID.randomUUID().toString())
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .build();

        Set<Role> roles = new HashSet<>(Collections.singleton(roleRepository.findByName(RoleType.CANDIDATE)
                .orElseThrow(() -> new RuntimeException("Role not found: " + RoleType.CANDIDATE.name()))));
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        Profile profile = Profile.builder()
                .firstName(registerUserDto.getFirstName())
                .lastName(registerUserDto.getLastName())
                .dayOfBirth(LocalDateTime.parse(registerUserDto.getDayOfBirth(), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                .address(registerUserDto.getAddress())
                .avatar(registerUserDto.getAvatar())
                .description(registerUserDto.getDescription())
                .user(savedUser)
                .build();

        profileRepository.save(profile);

        return savedUser;
    }

    public User authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()
                )
        );

        return userRepository.findByUsername(input.getUsername())
                .orElseThrow();
    }
}