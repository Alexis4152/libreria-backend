package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.response.StoreConfigResponse;
import com.libreria.ecommerce.service.StoreConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/store-config")
@RequiredArgsConstructor
public class PublicStoreConfigController {

    private final StoreConfigService storeConfigService;

    @GetMapping
    public ApiResponse<StoreConfigResponse> get() {
        return ApiResponse.ok(storeConfigService.get());
    }
}
