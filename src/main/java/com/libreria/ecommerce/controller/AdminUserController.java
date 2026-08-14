package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.PageResponse;
import com.libreria.ecommerce.dto.response.UserResponse;
import com.libreria.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = userService.adminListCustomers(
                q, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ApiResponse.ok(PageResponse.of(result));
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse<UserResponse> deactivate(@PathVariable Long id) {
        return ApiResponse.ok(userService.adminDeactivate(id), "Cliente desactivado");
    }
}
