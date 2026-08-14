package com.libreria.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentResponse {
    private String cardLast4;
    private String cardBrand;
    private String status;
    private String authorizationCode;
    private String transactionId;
}
