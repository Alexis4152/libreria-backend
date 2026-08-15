package com.libreria.ecommerce.service;

import com.libreria.ecommerce.entity.Order;
import com.libreria.ecommerce.enums.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Construye el filtro dinámico del listado admin de pedidos aplicado siempre desde el Backend. */
public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<Order> search(OrderStatus status, String like, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (like != null && !like.isBlank()) {
                Predicate byFolio = cb.like(cb.lower(root.get("folio")), like);
                Predicate byFirstName = cb.like(cb.lower(root.get("buyerFirstName")), like);
                Predicate byLastName = cb.like(cb.lower(root.get("buyerLastName")), like);
                Predicate byEmail = cb.like(cb.lower(root.get("buyerEmail")), like);
                predicates.add(cb.or(byFolio, byFirstName, byLastName, byEmail));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), dateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
