package com.libreria.ecommerce.repository;

import com.libreria.ecommerce.entity.Order;
import com.libreria.ecommerce.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByFolio(String folio);
    Page<Order> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Order> findByIdAndUser_Id(Long id, Long userId);
    long countByStatus(OrderStatus status);

    @Query("""
            SELECT o FROM Order o
            WHERE (:status IS NULL OR o.status = :status)
            AND (:like IS NULL OR LOWER(o.folio) LIKE :like
                 OR LOWER(o.buyerFirstName) LIKE :like OR LOWER(o.buyerLastName) LIKE :like
                 OR LOWER(o.buyerEmail) LIKE :like)
            AND (:dateFrom IS NULL OR o.createdAt >= :dateFrom)
            AND (:dateTo IS NULL OR o.createdAt <= :dateTo)
            """)
    Page<Order> search(@Param("status") OrderStatus status, @Param("like") String like,
                        @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo,
                        Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
           "WHERE o.paymentStatus = com.libreria.ecommerce.enums.PaymentStatus.APROBADO " +
           "AND o.createdAt >= :from")
    BigDecimal sumApprovedSince(LocalDateTime from);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
           "WHERE o.paymentStatus = com.libreria.ecommerce.enums.PaymentStatus.APROBADO")
    BigDecimal sumApprovedTotal();
}
