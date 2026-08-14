package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.request.CheckoutRequest;
import com.libreria.ecommerce.dto.response.CheckoutResponse;
import com.libreria.ecommerce.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Público (ver {@code SecurityConfig}: {@code /api/checkout/**} no exige autenticación) para
 * permitir compra como invitado; si el request trae un JWT válido, {@link
 * com.libreria.ecommerce.security.JwtAuthFilter} igual puebla el usuario autenticado en el
 * contexto de seguridad y el pedido queda asociado a esa cuenta.
 */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public ApiResponse<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ApiResponse.ok(checkoutService.checkout(request));
    }
}
