package com.libreria.ecommerce.dto.request;

import com.libreria.ecommerce.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private OrderStatus status;

    private String note;
}
