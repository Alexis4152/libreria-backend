package com.libreria.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StoreConfigResponse {
    private String storeName;
    private String legalName;
    private String rfc;
    private String address;
    private String phone;
    private String email;
    private String logoUrl;
    private String faviconUrl;
    private String primaryColor;
    private String secondaryColor;
    private String welcomeMessage;
    private String ticketMessage;
    private String socialLinks;
    private String shippingInfo;
    private String footerText;
}
