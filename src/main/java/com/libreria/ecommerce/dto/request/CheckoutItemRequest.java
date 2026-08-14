package com.libreria.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CheckoutItemRequest {

    @NotNull(message = "El libro es obligatorio")
    private Long bookId;

    @Positive(message = "La cantidad debe ser mayor a cero")
    private int quantity;
}
