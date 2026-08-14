package com.libreria.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardResponse {
    private long activeBooks;
    private long outOfStockBooks;
    private long totalOrders;
    private long pendingOrders;
    private BigDecimal salesToday;
    private BigDecimal salesTotal;
}
