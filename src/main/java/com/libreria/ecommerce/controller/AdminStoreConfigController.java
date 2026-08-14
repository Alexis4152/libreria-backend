package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.request.StoreConfigRequest;
import com.libreria.ecommerce.dto.response.StoreConfigResponse;
import com.libreria.ecommerce.service.StoreConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/store-config")
@RequiredArgsConstructor
public class AdminStoreConfigController {

    private final StoreConfigService storeConfigService;

    @GetMapping
    public ApiResponse<StoreConfigResponse> get() {
        return ApiResponse.ok(storeConfigService.get());
    }

    @PutMapping
    public ApiResponse<StoreConfigResponse> update(@Valid @RequestBody StoreConfigRequest request) {
        return ApiResponse.ok(storeConfigService.update(request), "Configuración actualizada");
    }

    @PostMapping(value = "/logo", consumes = "multipart/form-data")
    public ApiResponse<StoreConfigResponse> updateLogo(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(storeConfigService.updateLogo(file), "Logo actualizado");
    }

    @PostMapping(value = "/favicon", consumes = "multipart/form-data")
    public ApiResponse<StoreConfigResponse> updateFavicon(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(storeConfigService.updateFavicon(file), "Favicon actualizado");
    }
}
