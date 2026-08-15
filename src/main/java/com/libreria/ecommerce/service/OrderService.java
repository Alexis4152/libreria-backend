package com.libreria.ecommerce.service;

import com.libreria.ecommerce.dto.response.OrderDetailResponse;
import com.libreria.ecommerce.dto.response.OrderSummaryResponse;
import com.libreria.ecommerce.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderService {
    Page<OrderSummaryResponse> getMyOrders(Pageable pageable);
    OrderDetailResponse getMyOrderDetail(Long orderId);
    Page<OrderSummaryResponse> adminList(OrderStatus status, String q, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);
    OrderDetailResponse adminGetDetail(Long orderId);
    OrderDetailResponse adminUpdateStatus(Long orderId, OrderStatus newStatus, String note);
}
