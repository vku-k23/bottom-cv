package com.cnpm.bottomcv.config;

import com.cnpm.bottomcv.constant.AppConstant;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.dto.RegisterUserDto;
import com.cnpm.bottomcv.exception.ResourceNotFoundException;
import com.cnpm.bottomcv.model.Role;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.RoleRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import com.cnpm.bottomcv.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Component
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            @Lazy AuthenticationService authenticationService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        if (roleRepository.count() == 0) {
            Role roleUser = Role.builder()
                    .name(RoleType.CANDIDATE)
                    .build();

            Role roleAdmin = Role.builder()
                    .name(RoleType.ADMIN)
                    .build();

            Role roleModerator = Role.builder()
                    .name(RoleType.EMPLOYER)
                    .build();

            roleRepository.save(roleUser);
            roleRepository.save(roleAdmin);
            roleRepository.save(roleModerator);

            log.info("Initialized default roles in the database.");
        }

        if (!userRepository.existsByUsername(AppConstant.ADMIN_USERNAME)) {
            Role roleAdmin = roleRepository.findByName(RoleType.ADMIN)
                    .orElseThrow(() -> new ResourceNotFoundException("Role id", "id", RoleType.ADMIN.toString()));

            RegisterUserDto registerUserDto = RegisterUserDto.builder()
                    .username(AppConstant.ADMIN_USERNAME)
                    .password(AppConstant.ADMIN_USERNAME)
                    .firstName("Admin")
                    .lastName("User")
                    .dayOfBirth(LocalDate.of(1990, 1, 1))
                    .email("admin@gmail.com")
                    .phoneNumber("0123456789")
                    .build();

            User user = authenticationService.signup(registerUserDto);
            Set<Role> roles = user.getRoles();
            roles.add(roleAdmin);
            user.setRoles(roles);

            userRepository.save(user);

            log.info("Initialized default admin user in the database.");
        } else {
            log.warn("Roles and admin user already exist in the database.");
        }
    }
}
