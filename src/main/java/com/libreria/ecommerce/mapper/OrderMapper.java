package com.libreria.ecommerce.mapper;

import com.libreria.ecommerce.dto.response.OrderDetailResponse;
import com.libreria.ecommerce.dto.response.OrderItemResponse;
import com.libreria.ecommerce.dto.response.OrderSummaryResponse;
import com.libreria.ecommerce.dto.response.PaymentResponse;
import com.libreria.ecommerce.entity.Order;
import com.libreria.ecommerce.entity.OrderItem;
import com.libreria.ecommerce.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .bookId(item.getBook().getId())
                .sku(item.getSku())
                .title(item.getTitle())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    public OrderSummaryResponse toSummary(Order order, int itemCount) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .folio(order.getFolio())
                .buyerFullName(order.getBuyerFirstName() + " " + order.getBuyerLastName())
                .total(order.getTotal())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .itemCount(itemCount)
                .createdAt(order.getCreatedAt())
                .build();
    }

    public OrderDetailResponse toDetail(Order order, List<OrderItem> items, Payment payment) {
        return OrderDetailResponse.builder()
                .id(order.getId())
                .folio(order.getFolio())
                .buyerFirstName(order.getBuyerFirstName())
                .buyerLastName(order.getBuyerLastName())
                .buyerEmail(order.getBuyerEmail())
                .buyerPhone(order.getBuyerPhone())
                .shippingAddressLine1(order.getShippingAddressLine1())
                .shippingAddressLine2(order.getShippingAddressLine2())
                .shippingCity(order.getShippingCity())
                .shippingState(order.getShippingState())
                .shippingPostalCode(order.getShippingPostalCode())
                .shippingCountry(order.getShippingCountry())
                .subtotal(order.getSubtotal())
                .total(order.getTotal())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .items(items.stream().map(this::toItemResponse).toList())
                .payment(payment != null ? PaymentResponse.builder()
                        .cardLast4(payment.getCardLast4())
                        .cardBrand(payment.getCardBrand())
                        .status(payment.getStatus().name())
                        .authorizationCode(payment.getAuthorizationCode())
                        .transactionId(payment.getTransactionId())
                        .build() : null)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
