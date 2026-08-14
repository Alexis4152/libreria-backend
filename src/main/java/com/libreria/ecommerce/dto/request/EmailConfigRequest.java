package com.libreria.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailConfigRequest {

    @NotNull(message = "Indica si el envío de correo está habilitado")
    private boolean enabled;

    @NotBlank(message = "El host SMTP es obligatorio")
    private String smtpHost;

    @NotNull(message = "El puerto SMTP es obligatorio")
    private Integer smtpPort;

    private String smtpUsername;

    // Si viene vacío/null, se conserva la contraseña ya guardada (no se sobreescribe con "").
    private String smtpPassword;

    private String fromAddress;
}
