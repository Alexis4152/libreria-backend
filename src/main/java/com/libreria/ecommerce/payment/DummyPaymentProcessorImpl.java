package com.libreria.ecommerce.payment;

import com.libreria.ecommerce.dto.request.CardRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simula el procesamiento de un pago con tarjeta, sin integrar ninguna pasarela real. Nunca
 * recibe ni almacena el número completo ni el CVV más allá de este método: solo se conservan
 * los últimos 4 dígitos y una marca simulada.
 * <p>
 * Modo {@code app.payment.dummy-mode}: {@code AUTO} aprueba si el último dígito del número de
 * tarjeta es par y rechaza si es impar (convención de tarjetas de prueba); {@code
 * ALWAYS_APPROVE}/{@code ALWAYS_REJECT} fuerzan el resultado para pruebas manuales.
 */
@Service
public class DummyPaymentProcessorImpl implements PaymentProcessor {

    @Value("${app.payment.dummy-mode}")
    private String mode;

    @Override
    public PaymentResult charge(CardRequest card, BigDecimal amount) {
        String digits = card.getCardNumber();
        boolean approved = switch (mode) {
            case "ALWAYS_APPROVE" -> true;
            case "ALWAYS_REJECT" -> false;
            default -> (digits.charAt(digits.length() - 1) - '0') % 2 == 0;
        };

        String transactionId = "DUMMY-" + UUID.randomUUID();
        String last4 = digits.substring(digits.length() - 4);
        String brand = guessBrand(digits);
        String authCode = approved ? "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() : null;

        return new PaymentResult(approved, transactionId, authCode, last4, brand, amount);
    }

    private String guessBrand(String cardNumber) {
        char first = cardNumber.charAt(0);
        return switch (first) {
            case '4' -> "VISA";
            case '5' -> "MASTERCARD";
            case '3' -> "AMEX";
            default -> "OTRA";
        };
    }
}
