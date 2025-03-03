package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.dto.response.UserResponse;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Users API", description = "The API of users just for admin role")
@RequestMapping(value = "/api/users", produces = {MediaType.APPLICATION_JSON_VALUE})
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<List<User>> allUsers() {
        List<User> users = userService.allUsers();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(users);
    }

//    @PostMapping("/")
//    public ResponseEntity<UserResponse> createUser(User user) {
//        UserRepository newUser = userService.createUser(user);
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(newUser);
//    }
}