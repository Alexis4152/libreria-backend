package com.libreria.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String bio;
}
