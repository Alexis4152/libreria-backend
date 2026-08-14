package com.libreria.ecommerce.service;

import com.libreria.ecommerce.entity.Book;
import com.libreria.ecommerce.enums.BookStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Construye el filtro dinámico de búsqueda de libros aplicado siempre desde el Backend. */
public final class BookSpecifications {

    private BookSpecifications() {
    }

    public static Specification<Book> search(String keyword, Long categoryId, Long authorId, Long publisherId,
                                              BigDecimal minPrice, BigDecimal maxPrice, Boolean inStockOnly,
                                              Boolean featuredOnly) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("isActive")));
            predicates.add(cb.equal(root.get("status"), BookStatus.ACTIVE));

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                Join<Object, Object> authorJoin = root.join("authors", JoinType.LEFT);
                Predicate byTitle = cb.like(cb.lower(root.get("title")), like);
                Predicate bySubtitle = cb.like(cb.lower(root.get("subtitle")), like);
                Predicate byIsbn = cb.like(cb.lower(root.get("isbn")), like);
                Predicate byDescription = cb.like(cb.lower(root.get("descriptionShort")), like);
                Predicate byAuthor = cb.like(cb.lower(authorJoin.get("name")), like);
                Predicate byPublisher = cb.like(cb.lower(root.join("publisher", JoinType.LEFT).get("name")), like);
                Predicate byCategory = cb.like(cb.lower(root.join("category", JoinType.LEFT).get("name")), like);
                predicates.add(cb.or(byTitle, bySubtitle, byIsbn, byDescription, byAuthor, byPublisher, byCategory));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (authorId != null) {
                predicates.add(cb.equal(root.join("authors", JoinType.LEFT).get("id"), authorId));
            }
            if (publisherId != null) {
                predicates.add(cb.equal(root.get("publisher").get("id"), publisherId));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (Boolean.TRUE.equals(inStockOnly)) {
                predicates.add(cb.greaterThan(root.get("stock"), 0));
            }
            if (Boolean.TRUE.equals(featuredOnly)) {
                predicates.add(cb.isTrue(root.get("isFeatured")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
