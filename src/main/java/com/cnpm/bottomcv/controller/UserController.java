package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.request.UserRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.UserResponse;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users API", description = "The API of users just for admin role")
@RequestMapping(value = "/api/back/users", produces = {MediaType.APPLICATION_JSON_VALUE})
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<ListResponse<UserResponse>> allUsers(
            @RequestParam(required = false) int pageNo,
            @RequestParam(required = false) int pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortType
    ) {
        ListResponse<UserResponse> users = userService.allUsers(pageNo, pageSize, sortBy, sortType);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    @PostMapping("/")
    public ResponseEntity<UserResponse> createUser(UserRequest userRequest) {
        UserResponse newUser = userService.createUser(userRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(newUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, UserRequest userRequest) {
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}