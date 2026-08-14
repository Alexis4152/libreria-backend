package com.libreria.ecommerce.service;

import com.libreria.ecommerce.dto.response.OrderDetailResponse;
import com.libreria.ecommerce.dto.response.OrderSummaryResponse;
import com.libreria.ecommerce.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Page<OrderSummaryResponse> getMyOrders(Pageable pageable);
    OrderDetailResponse getMyOrderDetail(Long orderId);
    Page<OrderSummaryResponse> adminList(OrderStatus status, String q, Pageable pageable);
    OrderDetailResponse adminGetDetail(Long orderId);
    OrderDetailResponse adminUpdateStatus(Long orderId, OrderStatus newStatus, String note);
}
