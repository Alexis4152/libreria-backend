package com.libreria.ecommerce.repository;

import com.libreria.ecommerce.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByIsActiveTrueOrderByNameAsc();
    Page<Author> findByIsActiveTrue(Pageable pageable);
    Page<Author> findByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
