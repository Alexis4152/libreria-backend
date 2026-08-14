package com.libreria.ecommerce.repository;

import com.libreria.ecommerce.entity.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailConfigRepository extends JpaRepository<EmailConfig, Long> {
}
