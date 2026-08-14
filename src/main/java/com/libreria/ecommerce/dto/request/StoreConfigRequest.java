package com.libreria.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreConfigRequest {

    @NotBlank(message = "El nombre comercial es obligatorio")
    private String storeName;

    private String legalName;
    private String rfc;
    private String address;
    private String phone;
    private String email;

    @NotBlank(message = "El color primario es obligatorio")
    private String primaryColor;

    @NotBlank(message = "El color secundario es obligatorio")
    private String secondaryColor;

    private String welcomeMessage;
    private String ticketMessage;
    private String socialLinks;
    private String shippingInfo;
    private String footerText;
}
