package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.response.AuthorResponse;
import com.libreria.ecommerce.dto.response.CategoryResponse;
import com.libreria.ecommerce.dto.response.PublisherResponse;
import com.libreria.ecommerce.service.AuthorService;
import com.libreria.ecommerce.service.CategoryService;
import com.libreria.ecommerce.service.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final CategoryService categoryService;
    private final AuthorService authorService;
    private final PublisherService publisherService;

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> categories() {
        return ApiResponse.ok(categoryService.listActive());
    }

    @GetMapping("/authors")
    public ApiResponse<List<AuthorResponse>> authors() {
        return ApiResponse.ok(authorService.listActive());
    }

    @GetMapping("/publishers")
    public ApiResponse<List<PublisherResponse>> publishers() {
        return ApiResponse.ok(publisherService.listActive());
    }
}
