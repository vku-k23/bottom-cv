package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.TimeFormat;
import com.cnpm.bottomcv.dto.request.UserFilterRequest;
import com.cnpm.bottomcv.dto.request.UserRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ProfileResponse;
import com.cnpm.bottomcv.dto.response.RoleResponse;
import com.cnpm.bottomcv.dto.response.UserResponse;
import com.cnpm.bottomcv.exception.ResourceAlreadyExistException;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Profile;
import com.cnpm.bottomcv.model.Role;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.ProfileRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserServiceWithFilterImpl userServiceWithFilter;

    @Override
    public ListResponse<UserResponse> allUsers(int pageNo, int pageSize, String sortBy, String sortType) {
        Sort sortObj = sortBy.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sortObj);
        Page<User> pageProfiles = userRepository.findAll(pageable);
        List<User> users = pageProfiles.getContent();

        return ListResponse.<UserResponse>builder()
                .data(mapToUserResponseList(users))
                .pageNo(pageProfiles.getNumber())
                .pageSize(pageProfiles.getSize())
                .totalElements((int) pageProfiles.getTotalElements())
                .totalPages(pageProfiles.getTotalPages())
                .isLast(pageProfiles.isLast())
                .build();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User id", "id", id.toString()));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new ResourceAlreadyExistException("Username already exists!");
        }
        if (profileRepository.existsByEmail(userRequest.getEmail())) {
            throw new ResourceAlreadyExistException("Email already exists!");
        }

        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setUserCode(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRoles(userRequest.getRoles());

        User savedUser = userRepository.save(user);

        Profile profile = Profile.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .dayOfBirth(userRequest.getDayOfBirth())
                .address(userRequest.getAddress())
                .phoneNumber(userRequest.getPhoneNumber())
                .email(userRequest.getEmail())
                .avatar(userRequest.getAvatar())
                .description(userRequest.getDescription())
                .user(savedUser)
                .build();

        profileRepository.save(profile);

        return mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceAlreadyExistException("User not found!"));

        if (userRequest.getUsername() != null && !userRequest.getUsername().isEmpty()) {
            if (userRepository.existsByUsername(userRequest.getUsername())) {
                throw new ResourceAlreadyExistException("Username already exists!");
            }
            user.setUsername(userRequest.getUsername());
        }
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        if (userRequest.getEmail() != null && !userRequest.getEmail().isEmpty()) {
            if (profileRepository.existsByEmail(userRequest.getEmail())) {
                throw new ResourceAlreadyExistException("Email already exists!");
            }
            user.getProfile().setEmail(userRequest.getEmail());
        }
        if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
            user.setRoles(userRequest.getRoles());
        }

        Profile profile = user.getProfile();
        profile.setFirstName(userRequest.getFirstName());
        profile.setLastName(userRequest.getLastName());
        profile.setDayOfBirth(userRequest.getDayOfBirth());
        profile.setAddress(userRequest.getAddress());
        profile.setPhoneNumber(userRequest.getPhoneNumber());
        profile.setDescription(userRequest.getDescription());
        profile.setAvatar(userRequest.getAvatar());

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id.toString()));
        userRepository.delete(user);
    }

    @Override
    public ListResponse<UserResponse> getAllUsersWithFilter(UserFilterRequest filterRequest) {
        return userServiceWithFilter.getAllUsersWithFilter(filterRequest);
    }

    private List<UserResponse> mapToUserResponseList(List<User> users) {
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .userCode(user.getUserCode())
                .profile(mapToProfileResponse(user.getProfile()))
                .roles(mapToRoleResponse(user.getRoles()))
                .status(user.getStatus())
                .createdAt(user.getCreatedAt().format(DateTimeFormatter.ofPattern(TimeFormat.DATE_TIME_FORMAT)))
                .updatedAt(user.getUpdatedAt().format(DateTimeFormatter.ofPattern(TimeFormat.DATE_TIME_FORMAT)))
                .build();
    }

    private ProfileResponse mapToProfileResponse(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .dayOfBirth(profile.getDayOfBirth())
                .address(profile.getAddress())
                .phoneNumber(profile.getPhoneNumber())
                .email(profile.getEmail())
                .avatar(profile.getAvatar())
                .description(profile.getDescription())
                .createdAt(profile.getCreatedAt().format(DateTimeFormatter.ofPattern(TimeFormat.DATE_TIME_FORMAT)))
                .updatedAt(profile.getUpdatedAt().format(DateTimeFormatter.ofPattern(TimeFormat.DATE_TIME_FORMAT)))
                .build();
    }

    private Set<RoleResponse> mapToRoleResponse(Set<Role> roles) {
        return roles.stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build())
                .collect(Collectors.toSet());
    }
}
