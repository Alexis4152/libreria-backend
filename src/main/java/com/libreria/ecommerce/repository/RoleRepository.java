package com.libreria.ecommerce.repository;

import com.libreria.ecommerce.entity.Role;
import com.libreria.ecommerce.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
