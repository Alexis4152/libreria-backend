package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.dto.response.DashboardResponse;
import com.libreria.ecommerce.enums.OrderStatus;
import com.libreria.ecommerce.repository.BookRepository;
import com.libreria.ecommerce.repository.OrderRepository;
import com.libreria.ecommerce.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;

    @Override
    public DashboardResponse getSummary() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        return DashboardResponse.builder()
                .activeBooks(bookRepository.countByIsActiveTrue())
                .outOfStockBooks(bookRepository.countByIsActiveTrueAndStockLessThanEqual(0))
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByStatus(OrderStatus.PENDIENTE))
                .salesToday(orderRepository.sumApprovedSince(startOfToday))
                .salesTotal(orderRepository.sumApprovedTotal())
                .build();
    }
}
