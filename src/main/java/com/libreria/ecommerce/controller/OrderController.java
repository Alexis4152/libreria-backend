package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.PageResponse;
import com.libreria.ecommerce.dto.response.OrderDetailResponse;
import com.libreria.ecommerce.dto.response.OrderSummaryResponse;
import com.libreria.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<PageResponse<OrderSummaryResponse>> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = orderService.getMyOrders(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ApiResponse.ok(PageResponse.of(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> myOrderDetail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getMyOrderDetail(id));
    }
}
