package com.libreria.ecommerce.repository;

import com.libreria.ecommerce.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findBySkuAndIsActiveTrue(String sku);

    @EntityGraph(attributePaths = {"category", "publisher", "images"})
    Optional<Book> findByIdAndIsActiveTrue(Long id);

    boolean existsBySku(String sku);

    long countByIsActiveTrue();

    long countByIsActiveTrueAndStockLessThanEqual(int stock);

    // Bloqueo pesimista: evita que dos checkouts concurrentes vendan el mismo último
    // ejemplar (ver GlobalExceptionHandler/OutOfStockException y CheckoutService).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdForUpdate(Long id);

    List<Book> findTop8ByIsFeaturedTrueAndIsActiveTrueAndStatus(
            com.libreria.ecommerce.enums.BookStatus status);

    @Query("""
            SELECT b FROM Book b
            WHERE b.isActive = true AND b.status = 'ACTIVE' AND b.category.id = :categoryId
              AND b.id <> :excludeId
            """)
    Page<Book> findRelated(Long categoryId, Long excludeId, Pageable pageable);
}
