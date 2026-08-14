package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.dto.request.CheckoutItemRequest;
import com.libreria.ecommerce.dto.request.CheckoutRequest;
import com.libreria.ecommerce.dto.request.ShippingAddressRequest;
import com.libreria.ecommerce.dto.response.CheckoutResponse;
import com.libreria.ecommerce.entity.*;
import com.libreria.ecommerce.enums.OrderStatus;
import com.libreria.ecommerce.enums.PaymentStatus;
import com.libreria.ecommerce.exception.BusinessException;
import com.libreria.ecommerce.exception.OutOfStockException;
import com.libreria.ecommerce.exception.ResourceNotFoundException;
import com.libreria.ecommerce.mapper.OrderMapper;
import com.libreria.ecommerce.payment.PaymentProcessor;
import com.libreria.ecommerce.payment.PaymentResult;
import com.libreria.ecommerce.repository.*;
import com.libreria.ecommerce.security.SecurityUtils;
import com.libreria.ecommerce.service.CheckoutService;
import com.libreria.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final BookRepository bookRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentProcessor paymentProcessor;
    private final OrderMapper orderMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        User currentUser = SecurityUtils.getCurrentUserOrNull();
        Order order = new Order();
        order.setUser(currentUser);
        order.setBuyerFirstName(request.getBuyerFirstName());
        order.setBuyerLastName(request.getBuyerLastName());
        order.setBuyerEmail(request.getBuyerEmail());
        order.setBuyerPhone(request.getBuyerPhone());
        applyShippingAddress(order, request.getShippingAddress(), currentUser);
        order.setStatus(OrderStatus.PENDIENTE);
        order.setPaymentStatus(PaymentStatus.PENDIENTE);
        order.setCreatedBy(currentUser);

        // Bloquea cada libro (SELECT ... FOR UPDATE) y valida stock ANTES de crear nada, para
        // que dos checkouts concurrentes no puedan vender el mismo último ejemplar y para que,
        // si el carrito es inválido, no quede ningún pedido huérfano persistido.
        List<Book> lockedBooks = new ArrayList<>();
        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CheckoutItemRequest itemReq : request.getItems()) {
            Book book = bookRepository.findByIdForUpdate(itemReq.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado: " + itemReq.getBookId()));
            if (!Boolean.TRUE.equals(book.getIsActive()) || book.getStatus().name().equals("INACTIVE")) {
                throw new BusinessException("El libro '" + book.getTitle() + "' ya no está disponible");
            }
            if (book.getStock() < itemReq.getQuantity()) {
                throw new OutOfStockException("No hay suficiente stock de '" + book.getTitle() + "'");
            }
            BigDecimal unitPrice = book.getPromoPrice() != null ? book.getPromoPrice() : book.getPrice();
            BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(lineSubtotal);

            lockedBooks.add(book);
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .book(book)
                    .sku(book.getSku())
                    .title(book.getTitle())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(lineSubtotal)
                    .build();
            items.add(item);
        }

        order.setSubtotal(subtotal);
        order.setTotal(subtotal);
        order.setFolio(generateFolio());
        order = orderRepository.save(order);
        for (OrderItem item : items) {
            item.setOrder(order);
        }
        orderItemRepository.saveAll(items);

        PaymentResult result = paymentProcessor.charge(request.getCard(), order.getTotal());

        Payment payment = Payment.builder()
                .order(order)
                .cardLast4(result.cardLast4())
                .cardBrand(result.cardBrand())
                .transactionId(result.transactionId())
                .status(result.approved() ? PaymentStatus.APROBADO : PaymentStatus.RECHAZADO)
                .authorizationCode(result.authorizationCode())
                .build();
        payment = paymentRepository.save(payment);

        OrderStatus previousStatus = order.getStatus();
        if (result.approved()) {
            for (int i = 0; i < lockedBooks.size(); i++) {
                Book book = lockedBooks.get(i);
                book.setStock(book.getStock() - items.get(i).getQuantity());
                bookRepository.save(book);
            }
            order.setStatus(OrderStatus.PAGADO);
            order.setPaymentStatus(PaymentStatus.APROBADO);
        } else {
            order.setStatus(OrderStatus.CANCELADO);
            order.setPaymentStatus(PaymentStatus.RECHAZADO);
        }
        order = orderRepository.save(order);

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .previousStatus(previousStatus)
                .newStatus(order.getStatus())
                .changedBy(currentUser)
                .note(result.approved() ? "Pago aprobado" : "Pago rechazado")
                .build());

        // El correo va al email capturado en el checkout: el de la cuenta si el comprador
        // inició sesión, o el que haya escrito como invitado — nunca falla el checkout si el
        // envío falla (ver EmailServiceImpl, atrapa sus propias excepciones).
        if (result.approved()) {
            emailService.sendOrderApproved(order, items, payment);
        } else {
            emailService.sendOrderRejected(order);
        }

        return CheckoutResponse.builder()
                .approved(result.approved())
                .message(result.approved() ? "Pago aprobado" : "El pago fue rechazado, intenta con otra tarjeta")
                .order(orderMapper.toDetail(order, items, payment))
                .build();
    }

    private void applyShippingAddress(Order order, ShippingAddressRequest req, User currentUser) {
        if (req != null && req.getAddressId() != null) {
            if (currentUser == null) {
                throw new BusinessException("Debes iniciar sesión para usar una dirección guardada");
            }
            Address saved = addressRepository.findById(req.getAddressId())
                    .filter(a -> a.getUser().getId().equals(currentUser.getId()) && Boolean.TRUE.equals(a.getIsActive()))
                    .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada: " + req.getAddressId()));
            order.setShippingAddressLine1(saved.getAddressLine1());
            order.setShippingAddressLine2(saved.getAddressLine2());
            order.setShippingCity(saved.getCity());
            order.setShippingState(saved.getState());
            order.setShippingPostalCode(saved.getPostalCode());
            order.setShippingCountry(saved.getCountry());
            return;
        }
        if (req == null || req.getAddressLine1() == null || req.getCity() == null) {
            throw new BusinessException("La dirección de envío es obligatoria");
        }
        order.setShippingAddressLine1(req.getAddressLine1());
        order.setShippingAddressLine2(req.getAddressLine2());
        order.setShippingCity(req.getCity());
        order.setShippingState(req.getState());
        order.setShippingPostalCode(req.getPostalCode());
        order.setShippingCountry(req.getCountry());
    }

    private String generateFolio() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + date + "-" + suffix;
    }
}
