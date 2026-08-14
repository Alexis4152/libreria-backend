package com.libreria.ecommerce.service;

import com.libreria.ecommerce.entity.Order;
import com.libreria.ecommerce.entity.OrderItem;
import com.libreria.ecommerce.entity.Payment;

import java.util.List;

public interface EmailService {

    /** Envía el ticket de compra al correo del cliente (registrado o el capturado como invitado). */
    void sendOrderApproved(Order order, List<OrderItem> items, Payment payment);

    /** Avisa al cliente que su pago fue rechazado, sin ticket de compra. */
    void sendOrderRejected(Order order);
}
