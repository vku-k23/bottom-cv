package com.cnpm.bottomcv.config;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.model.Role;
import com.cnpm.bottomcv.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
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

            System.out.println("Initialized default roles in the database.");
        } else {
            System.out.println("Roles already exist in the database.");
        }
    }
}
