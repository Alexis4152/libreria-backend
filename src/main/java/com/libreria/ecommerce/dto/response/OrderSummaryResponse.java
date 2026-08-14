package com.libreria.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderSummaryResponse {
    private Long id;
    private String folio;
    private String buyerFullName;
    private BigDecimal total;
    private String status;
    private String paymentStatus;
    private int itemCount;
    private LocalDateTime createdAt;
}
