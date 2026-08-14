package com.libreria.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Nunca incluye la contraseña real: solo indica si ya hay una guardada. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EmailConfigResponse {
    private boolean enabled;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String fromAddress;
    private boolean passwordConfigured;
}
