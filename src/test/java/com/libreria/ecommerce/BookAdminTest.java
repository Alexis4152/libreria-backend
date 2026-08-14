package com.libreria.ecommerce;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.request.BookRequest;
import com.libreria.ecommerce.dto.request.StockAdjustRequest;
import com.libreria.ecommerce.dto.response.BookDetailResponse;
import com.libreria.ecommerce.entity.Category;
import com.libreria.ecommerce.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BookAdminTest extends AbstractIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    private ResponseEntity<ApiResponse<BookDetailResponse>> createBook(String token, Category category, String sku) {
        BookRequest req = new BookRequest();
        req.setSku(sku);
        req.setTitle("Libro de prueba " + sku);
        req.setCategoryId(category.getId());
        req.setPrice(new BigDecimal("250.00"));
        req.setStock(10);

        return restTemplate.exchange(
                baseUrl("/api/admin/books"), HttpMethod.POST,
                new HttpEntity<>(req, authHeaders(token)),
                new ParameterizedTypeReference<>() {});
    }

    @Test
    void createUpdateAndSoftDeleteBook() {
        String token = adminToken();
        Category category = createCategory("Categoria-" + System.nanoTime());
        String sku = "SKU-CRUD-" + System.nanoTime();

        var createResponse = createBook(token, category, sku);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long bookId = createResponse.getBody().getData().getId();

        // Modificación
        BookRequest update = new BookRequest();
        update.setSku(sku);
        update.setTitle("Título actualizado");
        update.setCategoryId(category.getId());
        update.setPrice(new BigDecimal("299.00"));
        update.setStock(10);

        var updateResponse = restTemplate.exchange(
                baseUrl("/api/admin/books/" + bookId), HttpMethod.PUT,
                new HttpEntity<>(update, authHeaders(token)),
                new ParameterizedTypeReference<ApiResponse<BookDetailResponse>>() {});
        assertThat(updateResponse.getBody().getData().getTitle()).isEqualTo("Título actualizado");

        // Borrado lógico: no debe aparecer más en el catálogo público...
        restTemplate.exchange(baseUrl("/api/admin/books/" + bookId), HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(token)), Void.class);

        ResponseEntity<String> publicDetail = restTemplate.getForEntity(
                baseUrl("/api/public/books/" + bookId), String.class);
        assertThat(publicDetail.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // ...pero el registro sigue existiendo en BD (no se borra físicamente).
        assertThat(bookRepository.findById(bookId)).isPresent();
        assertThat(bookRepository.findById(bookId).orElseThrow().getIsActive()).isFalse();
    }

    @Test
    void stockCannotGoNegativeAndAdjustsCorrectly() {
        String token = adminToken();
        Category category = createCategory("Categoria-" + System.nanoTime());
        Long bookId = createBook(token, category, "SKU-STOCK-" + System.nanoTime()).getBody().getData().getId();

        StockAdjustRequest negative = new StockAdjustRequest();
        negative.setStock(-5);
        ResponseEntity<String> negativeResponse = restTemplate.exchange(
                baseUrl("/api/admin/books/" + bookId + "/stock"), HttpMethod.PATCH,
                new HttpEntity<>(negative, authHeaders(token)), String.class);
        assertThat(negativeResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        StockAdjustRequest valid = new StockAdjustRequest();
        valid.setStock(42);
        var response = restTemplate.exchange(
                baseUrl("/api/admin/books/" + bookId + "/stock"), HttpMethod.PATCH,
                new HttpEntity<>(valid, authHeaders(token)),
                new ParameterizedTypeReference<ApiResponse<BookDetailResponse>>() {});
        assertThat(response.getBody().getData().getStock()).isEqualTo(42);
    }

    private boolean bookRepositoryHasRow(Long id) {
        return org.springframework.test.util.ReflectionTestUtils.invokeGetterMethod(this, "port") != null
                && true; // placeholder replaced below
    }
}
