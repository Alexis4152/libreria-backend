package com.libreria.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Forma ligera de un libro usada en listados, resultados de búsqueda y cards de producto. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookSummaryResponse {
    private Long id;
    private String sku;
    private String title;
    private String subtitle;
    private String authorNames;
    private String categoryName;
    private String publisherName;
    private BigDecimal price;
    private BigDecimal promoPrice;
    private Integer stock;
    private String coverImageUrl;
    private boolean featured;
    private boolean newRelease;
}
