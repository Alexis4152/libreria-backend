package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.PageResponse;
import com.libreria.ecommerce.dto.request.PublisherRequest;
import com.libreria.ecommerce.dto.response.PublisherResponse;
import com.libreria.ecommerce.service.PublisherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/publishers")
@RequiredArgsConstructor
public class AdminPublisherController {

    private final PublisherService publisherService;

    @GetMapping
    public ApiResponse<PageResponse<PublisherResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = publisherService.adminList(q, PageRequest.of(page, size, Sort.by("name")));
        return ApiResponse.ok(PageResponse.of(result));
    }

    @PostMapping
    public ApiResponse<PublisherResponse> create(@Valid @RequestBody PublisherRequest request) {
        return ApiResponse.ok(publisherService.create(request), "Editorial creada");
    }

    @PutMapping("/{id}")
    public ApiResponse<PublisherResponse> update(@PathVariable Long id, @Valid @RequestBody PublisherRequest request) {
        return ApiResponse.ok(publisherService.update(id, request), "Editorial actualizada");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        publisherService.deactivate(id);
        return ApiResponse.ok(null, "Editorial desactivada");
    }
}
