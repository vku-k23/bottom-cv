package com.cnpm.bottomcv.repository;

import com.cnpm.bottomcv.constant.RoleType;
import com.cnpm.bottomcv.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
