package com.libreria.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Formulario de tarjeta DUMMY. Nunca se persiste el número completo ni el CVV. */
@Data
public class CardRequest {

    @NotBlank(message = "El nombre del titular es obligatorio")
    private String holderName;

    @NotBlank(message = "El número de tarjeta es obligatorio")
    @Pattern(regexp = "\\d{13,19}", message = "El número de tarjeta no es válido")
    private String cardNumber;

    @Pattern(regexp = "0[1-9]|1[0-2]", message = "Mes de expiración inválido")
    private String expiryMonth;

    @Pattern(regexp = "\\d{2,4}", message = "Año de expiración inválido")
    private String expiryYear;

    @NotBlank(message = "El CVV es obligatorio")
    @Pattern(regexp = "\\d{3,4}", message = "CVV inválido")
    private String cvv;
}
