package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.request.EmailConfigRequest;
import com.libreria.ecommerce.dto.response.EmailConfigResponse;
import com.libreria.ecommerce.service.EmailConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/email-config")
@RequiredArgsConstructor
public class AdminEmailConfigController {

    private final EmailConfigService emailConfigService;

    @GetMapping
    public ApiResponse<EmailConfigResponse> get() {
        return ApiResponse.ok(emailConfigService.get());
    }

    @PutMapping
    public ApiResponse<EmailConfigResponse> update(@Valid @RequestBody EmailConfigRequest request) {
        return ApiResponse.ok(emailConfigService.update(request), "Configuración de correo actualizada");
    }
}
