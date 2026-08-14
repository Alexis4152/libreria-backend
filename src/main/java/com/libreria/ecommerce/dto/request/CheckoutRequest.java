package com.libreria.ecommerce.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {

    @NotEmpty(message = "El carrito no puede estar vacío")
    @Valid
    private List<CheckoutItemRequest> items;

    @NotBlank(message = "El nombre es obligatorio")
    private String buyerFirstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String buyerLastName;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    private String buyerEmail;

    @NotBlank(message = "El teléfono es obligatorio")
    private String buyerPhone;

    @Valid
    private ShippingAddressRequest shippingAddress;

    @Valid
    private CardRequest card;
}
