package com.libreria.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustRequest {

    @NotNull(message = "El nuevo stock es obligatorio")
    private Integer stock;
}
