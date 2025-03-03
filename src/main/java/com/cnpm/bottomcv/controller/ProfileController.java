package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.ProfileRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.ProfileResponse;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.service.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile API", description = "The API of profile just for authenticated user")
@RequestMapping(value = "/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/profile")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody ProfileRequest profileRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(profileService.updateProfile(currentUser.getId(), profileRequest));
    }

    @GetMapping("/back/profile")
    public ResponseEntity<ListResponse<ProfileResponse>> getAllProfiles(
            @RequestParam int pageNo,
            @RequestParam int pageSize,
            @RequestParam String sortBy,
            @RequestParam String sortType
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileService.allProfiles(pageNo, pageSize, sortBy, sortType));
    }

    @GetMapping("/back/profile/{id}")
    public ResponseEntity<ProfileResponse> getProfileById(@RequestParam Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileService.getProfileById(id));
    }

    @PostMapping("/back/profile")
    public ResponseEntity<ProfileResponse> createProfile(@Valid @RequestBody ProfileRequest profileRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(profileService.createProfile(profileRequest));
    }

    @PutMapping("/back/profile/{id}")
    public ResponseEntity<ProfileResponse> updateProfile(@PathVariable Long id,
                                                         @Valid @RequestBody ProfileRequest profileRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileService.updateProfile(id, profileRequest));
    }

    @DeleteMapping("/back/profile/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

}