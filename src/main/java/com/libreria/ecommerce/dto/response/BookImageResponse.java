package com.libreria.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookImageResponse {
    private Long id;
    private String url;
    private boolean primary;
    private int sortOrder;
    private String altText;
}
