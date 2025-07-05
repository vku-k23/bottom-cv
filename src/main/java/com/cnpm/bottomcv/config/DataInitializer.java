package com.cnpm.bottomcv.config;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.model.Role;
import com.cnpm.bottomcv.model.User;
import com.cnpm.bottomcv.repository.RoleRepository;
import com.cnpm.bottomcv.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
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
        
        if (!userRepository.existsByUsername("admin")) {
            Role roleAdmin = roleRepository.findByName(RoleType.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User adminUser = User.builder()
                    .username("admin")
                    .userCode("admin-001")
                    .password("admin123")
                    .roles(Set.of(roleAdmin))
                    .build();

            userRepository.save(adminUser);
            log.info("Initialized default admin user in the database.");
        } else {
            log.warn("Roles and admin user already exist in the database.");
        }
    }
}
