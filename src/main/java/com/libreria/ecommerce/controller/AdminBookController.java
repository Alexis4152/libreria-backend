package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.PageResponse;
import com.libreria.ecommerce.dto.request.BookRequest;
import com.libreria.ecommerce.dto.request.StockAdjustRequest;
import com.libreria.ecommerce.dto.response.BookDetailResponse;
import com.libreria.ecommerce.dto.response.BookImageResponse;
import com.libreria.ecommerce.dto.response.BookSummaryResponse;
import com.libreria.ecommerce.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final BookService bookService;

    @GetMapping
    public ApiResponse<PageResponse<BookSummaryResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = bookService.adminList(q, categoryId, PageRequest.of(page, size, Sort.by("title")));
        return ApiResponse.ok(PageResponse.of(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(bookService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<BookDetailResponse> create(@Valid @RequestBody BookRequest request) {
        return ApiResponse.ok(bookService.create(request), "Libro creado");
    }

    @PutMapping("/{id}")
    public ApiResponse<BookDetailResponse> update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ApiResponse.ok(bookService.update(id, request), "Libro actualizado");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        bookService.deactivate(id);
        return ApiResponse.ok(null, "Libro desactivado");
    }

    @PatchMapping("/{id}/stock")
    public ApiResponse<BookDetailResponse> adjustStock(@PathVariable Long id,
                                                         @Valid @RequestBody StockAdjustRequest request) {
        return ApiResponse.ok(bookService.adjustStock(id, request.getStock()), "Stock actualizado");
    }

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ApiResponse<BookImageResponse> addImage(@PathVariable Long id,
                                                     @RequestParam("file") MultipartFile file,
                                                     @RequestParam(defaultValue = "false") boolean primary) {
        return ApiResponse.ok(bookService.addImage(id, file, primary), "Imagen agregada");
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ApiResponse<Void> deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        bookService.deleteImage(id, imageId);
        return ApiResponse.ok(null, "Imagen eliminada");
    }
}
