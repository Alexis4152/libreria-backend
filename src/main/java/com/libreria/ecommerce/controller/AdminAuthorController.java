package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.PageResponse;
import com.libreria.ecommerce.dto.request.AuthorRequest;
import com.libreria.ecommerce.dto.response.AuthorResponse;
import com.libreria.ecommerce.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/authors")
@RequiredArgsConstructor
public class AdminAuthorController {

    private final AuthorService authorService;

    @GetMapping
    public ApiResponse<PageResponse<AuthorResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = authorService.adminList(q, PageRequest.of(page, size, Sort.by("name")));
        return ApiResponse.ok(PageResponse.of(result));
    }

    @PostMapping
    public ApiResponse<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
        return ApiResponse.ok(authorService.create(request), "Autor creado");
    }

    @PutMapping("/{id}")
    public ApiResponse<AuthorResponse> update(@PathVariable Long id, @Valid @RequestBody AuthorRequest request) {
        return ApiResponse.ok(authorService.update(id, request), "Autor actualizado");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        authorService.deactivate(id);
        return ApiResponse.ok(null, "Autor desactivado");
    }
}
