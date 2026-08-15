package com.libreria.ecommerce.repository;

import com.libreria.ecommerce.entity.Order;
import com.libreria.ecommerce.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByFolio(String folio);
    Page<Order> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Order> findByIdAndUser_Id(Long id, Long userId);
    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
           "WHERE o.paymentStatus = com.libreria.ecommerce.enums.PaymentStatus.APROBADO " +
           "AND o.createdAt >= :from")
    BigDecimal sumApprovedSince(LocalDateTime from);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
           "WHERE o.paymentStatus = com.libreria.ecommerce.enums.PaymentStatus.APROBADO")
    BigDecimal sumApprovedTotal();
}
