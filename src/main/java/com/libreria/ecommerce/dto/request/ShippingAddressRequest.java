package com.libreria.ecommerce.dto.request;

import lombok.Data;

/** Dirección de envío capturada en el checkout. Si {@code addressId} viene presente, se usa
 * una dirección guardada del usuario autenticado; de lo contrario se usan los campos inline
 * (obligatorio para invitados). */
@Data
public class ShippingAddressRequest {
    private Long addressId;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
