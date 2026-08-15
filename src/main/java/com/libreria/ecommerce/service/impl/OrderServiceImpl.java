package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.dto.response.OrderDetailResponse;
import com.libreria.ecommerce.dto.response.OrderSummaryResponse;
import com.libreria.ecommerce.entity.Order;
import com.libreria.ecommerce.entity.OrderItem;
import com.libreria.ecommerce.entity.OrderStatusHistory;
import com.libreria.ecommerce.entity.User;
import com.libreria.ecommerce.enums.OrderStatus;
import com.libreria.ecommerce.exception.BusinessException;
import com.libreria.ecommerce.exception.ResourceNotFoundException;
import com.libreria.ecommerce.mapper.OrderMapper;
import com.libreria.ecommerce.repository.OrderItemRepository;
import com.libreria.ecommerce.repository.OrderRepository;
import com.libreria.ecommerce.repository.OrderStatusHistoryRepository;
import com.libreria.ecommerce.repository.PaymentRepository;
import com.libreria.ecommerce.security.SecurityUtils;
import com.libreria.ecommerce.service.OrderService;
import com.libreria.ecommerce.service.OrderSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    // Transiciones de estado permitidas para el ADMIN; cualquier otra combinación se rechaza
    // para evitar, por ejemplo, "revivir" un pedido CANCELADO o retroceder un ENTREGADO.
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDIENTE, Set.of(OrderStatus.PAGADO, OrderStatus.CANCELADO),
            OrderStatus.PAGADO, Set.of(OrderStatus.PREPARANDO, OrderStatus.CANCELADO),
            OrderStatus.PREPARANDO, Set.of(OrderStatus.ENVIADO, OrderStatus.CANCELADO),
            OrderStatus.ENVIADO, Set.of(OrderStatus.ENTREGADO),
            OrderStatus.ENTREGADO, Set.of(),
            OrderStatus.CANCELADO, Set.of()
    );

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getMyOrders(Pageable pageable) {
        User user = currentUser();
        return orderRepository.findByUser_IdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(order -> orderMapper.toSummary(order, orderItemRepository.findByOrder_Id(order.getId()).size()));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getMyOrderDetail(Long orderId) {
        User user = currentUser();
        Order order = orderRepository.findByIdAndUser_Id(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + orderId));
        return buildDetail(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> adminList(OrderStatus status, String q, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        String like = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
        Page<Order> orders = orderRepository.findAll(OrderSpecifications.search(status, like, dateFrom, dateTo), pageable);
        return orders.map(order -> orderMapper.toSummary(order, orderItemRepository.findByOrder_Id(order.getId()).size()));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse adminGetDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + orderId));
        return buildDetail(order);
    }

    @Override
    @Transactional
    public OrderDetailResponse adminUpdateStatus(Long orderId, OrderStatus newStatus, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + orderId));

        OrderStatus previous = order.getStatus();
        if (!ALLOWED_TRANSITIONS.getOrDefault(previous, Set.of()).contains(newStatus)) {
            throw new BusinessException("No se puede cambiar el pedido de " + previous + " a " + newStatus);
        }

        User actor = SecurityUtils.getCurrentUserOrNull();
        order.setStatus(newStatus);
        order.setUpdatedBy(actor);
        order = orderRepository.save(order);

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .previousStatus(previous)
                .newStatus(newStatus)
                .changedBy(actor)
                .note(note)
                .build());

        return buildDetail(order);
    }

    private OrderDetailResponse buildDetail(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder_Id(order.getId());
        var payment = paymentRepository.findByOrder_Id(order.getId()).orElse(null);
        return orderMapper.toDetail(order, items, payment);
    }

    private User currentUser() {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesión activa");
        }
        return user;
    }
}
