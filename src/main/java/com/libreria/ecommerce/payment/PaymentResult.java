package com.libreria.ecommerce.payment;

import java.math.BigDecimal;

/** Resultado de un intento de cobro, sin importar el proveedor real detrás de {@link PaymentProcessor}. */
public record PaymentResult(
        boolean approved,
        String transactionId,
        String authorizationCode,
        String cardLast4,
        String cardBrand,
        BigDecimal amount
) {
}
