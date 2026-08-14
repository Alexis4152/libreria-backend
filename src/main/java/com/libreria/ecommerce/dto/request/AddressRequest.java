package com.libreria.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {

    private String label;

    @NotBlank(message = "El nombre del destinatario es obligatorio")
    private String recipientName;

    @NotBlank(message = "El teléfono es obligatorio")
    private String phone;

    @NotBlank(message = "La dirección es obligatoria")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    @NotBlank(message = "El estado es obligatorio")
    private String state;

    @NotBlank(message = "El código postal es obligatorio")
    private String postalCode;

    @NotBlank(message = "El país es obligatorio")
    private String country;

    // Nombrado sin prefijo "is" a propósito: Lombok genera setDefault()/isDefault() para un
    // campo "isDefault", lo que hace que Jackson exponga la propiedad JSON como "default" (no
    // "isDefault") y confunde al cliente. "defaultAddress" evita la ambigüedad por completo.
    private boolean defaultAddress;
}
