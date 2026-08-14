package com.libreria.ecommerce.repository;

import com.libreria.ecommerce.entity.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    List<Publisher> findByIsActiveTrueOrderByNameAsc();
    Page<Publisher> findByIsActiveTrue(Pageable pageable);
    Page<Publisher> findByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
