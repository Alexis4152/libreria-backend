package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.request.AddressRequest;
import com.libreria.ecommerce.dto.request.UserUpdateRequest;
import com.libreria.ecommerce.dto.response.AddressResponse;
import com.libreria.ecommerce.dto.response.UserResponse;
import com.libreria.ecommerce.service.AddressService;
import com.libreria.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe() {
        return ApiResponse.ok(userService.getMe());
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.ok(userService.updateMe(request), "Perfil actualizado");
    }

    @GetMapping("/me/addresses")
    public ApiResponse<List<AddressResponse>> myAddresses() {
        return ApiResponse.ok(addressService.listMine());
    }

    @PostMapping("/me/addresses")
    public ApiResponse<AddressResponse> addAddress(@Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok(addressService.create(request), "Dirección agregada");
    }

    @PutMapping("/me/addresses/{id}")
    public ApiResponse<AddressResponse> updateAddress(@PathVariable Long id,
                                                        @Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok(addressService.update(id, request), "Dirección actualizada");
    }

    @DeleteMapping("/me/addresses/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        addressService.delete(id);
        return ApiResponse.ok(null, "Dirección eliminada");
    }
}
