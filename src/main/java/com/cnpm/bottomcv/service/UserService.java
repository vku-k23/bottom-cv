package com.cnpm.bottomcv.service;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    public List<User> allUsers() {
        return new ArrayList<>(userRepository.findAll());
    }
}