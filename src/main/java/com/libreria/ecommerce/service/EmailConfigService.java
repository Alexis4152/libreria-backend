package com.libreria.ecommerce.service;

import com.libreria.ecommerce.dto.request.EmailConfigRequest;
import com.libreria.ecommerce.dto.response.EmailConfigResponse;
import com.libreria.ecommerce.entity.EmailConfig;

public interface EmailConfigService {
    EmailConfigResponse get();
    EmailConfigResponse update(EmailConfigRequest request);

    /** Usado internamente por EmailServiceImpl para armar el cliente SMTP. */
    EmailConfig getEntity();
}
