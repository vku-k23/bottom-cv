package com.cnpm.bottomcv.service.impl;

import com.cnpm.bottomcv.constant.TimeFormat;
import com.cnpm.bottomcv.dto.request.UserFilterRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ProfileResponse;
import com.cnpm.bottomcv.dto.response.RoleResponse;
import com.cnpm.bottomcv.dto.response.UserResponse;
import com.cnpm.bottomcv.model.Role;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceWithFilterImpl {
    private final UserRepository userRepository;

    public ListResponse<UserResponse> getAllUsersWithFilter(UserFilterRequest filterRequest) {
        log.info("Getting users with filter: {}", filterRequest);

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by username, email, or name
            if (filterRequest.getSearch() != null && !filterRequest.getSearch().isEmpty()) {
                String searchPattern = "%" + filterRequest.getSearch().toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("username")), searchPattern),
                        cb.like(cb.lower(root.join("profile").get("email")), searchPattern),
                        cb.like(cb.lower(root.join("profile").get("firstName")), searchPattern),
                        cb.like(cb.lower(root.join("profile").get("lastName")), searchPattern));
                predicates.add(searchPredicate);
            }

            // Filter by role - join roles table and check role name
            if (filterRequest.getRole() != null) {
                predicates.add(cb.equal(root.join("roles").get("name"), filterRequest.getRole()));
            }

            // Filter by status
            if (filterRequest.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filterRequest.getStatus()));
            }

            // Ensure distinct results when joining
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Sorting
        Sort sort = Sort.unsorted();
        if (filterRequest.getSortBy() != null && filterRequest.getSortDirection() != null) {
            sort = Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()),
                    filterRequest.getSortBy());
        }

        // Pagination
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, sort);

        // Use spec with findAll to apply filters
        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<User> users = userPage.getContent();

        return ListResponse.<UserResponse>builder()
                .data(mapToUserResponseList(users))
                .pageNo(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements((int) userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .isLast(userPage.isLast())
                .build();
    }

    private List<UserResponse> mapToUserResponseList(List<User> users) {
        return users.stream().map(this::mapToUserResponse).collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TimeFormat.DATE_TIME_FORMAT);

        return UserResponse.builder()
                .id(user.getId())
                .userCode(user.getUserCode())
                .username(user.getUsername())
                .roles(mapToRoleResponseSet(user.getRoles()))
                .profile(mapToProfileResponse(user.getProfile()))
                .status(user.getStatus())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().format(formatter) : null)
                .build();
    }

    private Set<RoleResponse> mapToRoleResponseSet(Set<Role> roles) {
        return roles.stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build())
                .collect(Collectors.toSet());
    }

    private ProfileResponse mapToProfileResponse(com.cnpm.bottomcv.model.Profile profile) {
        if (profile == null) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TimeFormat.DATE_TIME_FORMAT);

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
                .createdAt(profile.getCreatedAt() != null ? profile.getCreatedAt().format(formatter) : null)
                .updatedAt(profile.getUpdatedAt() != null ? profile.getUpdatedAt().format(formatter) : null)
                .build();
    }
}
