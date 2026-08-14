package com.libreria.ecommerce.payment;

import com.libreria.ecommerce.dto.request.CardRequest;

import java.math.BigDecimal;

/**
 * Puerto de cobro desacoplado del resto del flujo de pedidos. La única implementación de esta
 * primera versión es {@link DummyPaymentProcessorImpl}; sustituirla por Stripe/Mercado Pago/
 * PayPal/OpenPay más adelante no requiere tocar {@code CheckoutService} ni el modelo de
 * {@code Order}, solo proveer un bean distinto de esta interfaz.
 */
public interface PaymentProcessor {
    PaymentResult charge(CardRequest card, BigDecimal amount);
}
