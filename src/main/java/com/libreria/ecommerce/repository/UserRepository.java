package com.libreria.ecommerce.repository;

import com.libreria.ecommerce.entity.User;
import com.libreria.ecommerce.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByRole_Name(RoleName roleName, Pageable pageable);

    @Query("""
            SELECT u FROM User u WHERE u.role.name = :roleName
            AND (LOWER(u.firstName) LIKE :like OR LOWER(u.lastName) LIKE :like OR LOWER(u.email) LIKE :like)
            """)
    Page<User> searchByRole(@Param("roleName") RoleName roleName, @Param("like") String like, Pageable pageable);
}
