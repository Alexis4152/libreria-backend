package com.libreria.ecommerce.controller;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.PageResponse;
import com.libreria.ecommerce.dto.response.BookDetailResponse;
import com.libreria.ecommerce.dto.response.BookSummaryResponse;
import com.libreria.ecommerce.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/public/books")
@RequiredArgsConstructor
public class PublicBookController {

    private final BookService bookService;

    @GetMapping
    public ApiResponse<PageResponse<BookSummaryResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(required = false) Boolean featuredOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "relevance") String sort) {

        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        var result = bookService.search(q, categoryId, authorId, publisherId, minPrice, maxPrice,
                inStockOnly, featuredOnly, pageable);
        return ApiResponse.ok(PageResponse.of(result));
    }

    @GetMapping("/featured")
    public ApiResponse<List<BookSummaryResponse>> featured() {
        return ApiResponse.ok(bookService.getFeatured());
    }

    @GetMapping("/{id}")
    public ApiResponse<BookDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(bookService.getDetail(id));
    }

    private Sort resolveSort(String sort) {
        return switch (sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "title" -> Sort.by("title").ascending();
            case "newest" -> Sort.by("createdAt").descending();
            case "featured" -> Sort.by("isFeatured").descending().and(Sort.by("createdAt").descending());
            default -> Sort.by("createdAt").descending();
        };
    }
}
