package com.libreria.ecommerce.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ImageOrderRequest {

    @NotEmpty(message = "La lista de imagenes es obligatoria")
    private List<Long> imageIds;
}
