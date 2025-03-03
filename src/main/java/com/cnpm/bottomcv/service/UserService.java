package com.cnpm.bottomcv.service;

import com.cnpm.bottomcv.dto.request.UserRequest;
import com.cnpm.bottomcv.dto.response.ListResponse;
import com.cnpm.bottomcv.dto.response.UserResponse;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

public interface UserService {
    ListResponse<UserResponse> allUsers(int pageNo, int pageSize, String sortBy, String sortType);
    UserResponse getUserById(Long id);
    UserResponse createUser(UserRequest userRequest);
    UserResponse updateUser(Long id, UserRequest userRequest);
    void deleteUser(Long id);
}