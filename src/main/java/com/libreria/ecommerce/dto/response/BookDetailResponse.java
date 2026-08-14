package com.libreria.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookDetailResponse {
    private Long id;
    private String sku;
    private String isbn;
    private String title;
    private String subtitle;
    private String descriptionShort;
    private String descriptionLong;
    private CategoryResponse category;
    private PublisherResponse publisher;
    private List<AuthorResponse> authors;
    private BigDecimal price;
    private BigDecimal promoPrice;
    private Integer stock;
    private Integer publicationYear;
    private Integer pageCount;
    private String language;
    private String coverType;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private BigDecimal depthCm;
    private Integer weightGrams;
    private boolean featured;
    private boolean newRelease;
    private String status;
    private List<BookImageResponse> images;
    private List<BookSummaryResponse> relatedBooks;
}
