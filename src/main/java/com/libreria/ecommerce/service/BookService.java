package com.libreria.ecommerce.service;

import com.libreria.ecommerce.dto.request.BookRequest;
import com.libreria.ecommerce.dto.response.BookDetailResponse;
import com.libreria.ecommerce.dto.response.BookImageResponse;
import com.libreria.ecommerce.dto.response.BookSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface BookService {

    Page<BookSummaryResponse> search(String keyword, Long categoryId, Long authorId, Long publisherId,
                                      BigDecimal minPrice, BigDecimal maxPrice, Boolean inStockOnly,
                                      Boolean featuredOnly, Pageable pageable);

    List<BookSummaryResponse> getFeatured();

    BookDetailResponse getDetail(Long id);

    Page<BookSummaryResponse> adminList(String q, Long categoryId, Pageable pageable);

    BookDetailResponse create(BookRequest request);

    BookDetailResponse update(Long id, BookRequest request);

    void deactivate(Long id);

    BookDetailResponse adjustStock(Long id, Integer newStock);

    BookImageResponse addImage(Long bookId, MultipartFile file, boolean primary);

    void deleteImage(Long bookId, Long imageId);

    List<BookImageResponse> reorderImages(Long bookId, List<Long> imageIds);
}
