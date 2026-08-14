package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.dto.request.EmailConfigRequest;
import com.libreria.ecommerce.dto.response.EmailConfigResponse;
import com.libreria.ecommerce.entity.EmailConfig;
import com.libreria.ecommerce.repository.EmailConfigRepository;
import com.libreria.ecommerce.security.SecurityUtils;
import com.libreria.ecommerce.service.EmailConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailConfigServiceImpl implements EmailConfigService {

    private final EmailConfigRepository emailConfigRepository;

    @Override
    public EmailConfigResponse get() {
        return toResponse(getEntity());
    }

    @Override
    @Transactional
    public EmailConfigResponse update(EmailConfigRequest request) {
        EmailConfig config = getEntity();
        config.setEnabled(request.isEnabled());
        config.setSmtpHost(request.getSmtpHost());
        config.setSmtpPort(request.getSmtpPort());
        config.setSmtpUsername(request.getSmtpUsername());
        config.setFromAddress(request.getFromAddress());
        // Campo write-only: un valor vacío significa "no cambiar", nunca "borrar la guardada".
        if (request.getSmtpPassword() != null && !request.getSmtpPassword().isBlank()) {
            config.setSmtpPassword(request.getSmtpPassword());
        }
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return toResponse(emailConfigRepository.save(config));
    }

    @Override
    public EmailConfig getEntity() {
        return emailConfigRepository.findAll().stream().findFirst()
                .orElseGet(() -> emailConfigRepository.save(EmailConfig.builder()
                        .enabled(false)
                        .smtpHost("smtp.gmail.com")
                        .smtpPort(587)
                        .updatedAt(LocalDateTime.now())
                        .build()));
    }

    private EmailConfigResponse toResponse(EmailConfig config) {
        return EmailConfigResponse.builder()
                .enabled(Boolean.TRUE.equals(config.getEnabled()))
                .smtpHost(config.getSmtpHost())
                .smtpPort(config.getSmtpPort())
                .smtpUsername(config.getSmtpUsername())
                .fromAddress(config.getFromAddress())
                .passwordConfigured(config.getSmtpPassword() != null && !config.getSmtpPassword().isBlank())
                .build();
    }
}
