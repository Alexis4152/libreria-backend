package com.libreria.ecommerce;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.request.*;
import com.libreria.ecommerce.dto.response.BookDetailResponse;
import com.libreria.ecommerce.dto.response.CheckoutResponse;
import com.libreria.ecommerce.entity.Category;
import com.libreria.ecommerce.repository.BookRepository;
import com.libreria.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cubre el flujo de checkout de punta a punta contra la API real: creación de pedido,
 * cálculo de totales, pago dummy aprobado/rechazado y su efecto (o no) sobre el inventario.
 */
class CheckoutTest extends AbstractIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Long createBookWithStock(String token, Category category, int price, int stock, String skuSuffix) {
        BookRequest req = new BookRequest();
        req.setSku("SKU-CHK-" + skuSuffix + "-" + System.nanoTime());
        req.setTitle("Libro checkout " + skuSuffix);
        req.setCategoryId(category.getId());
        req.setPrice(new BigDecimal(price));
        req.setStock(stock);

        var response = restTemplate.exchange(
                baseUrl("/api/admin/books"), HttpMethod.POST,
                new HttpEntity<>(req, authHeaders(token)),
                new ParameterizedTypeReference<ApiResponse<BookDetailResponse>>() {});
        return response.getBody().getData().getId();
    }

    private CheckoutRequest buildCheckout(Long bookId, int quantity, String cardNumberEndingIn) {
        CheckoutItemRequest item = new CheckoutItemRequest();
        item.setBookId(bookId);
        item.setQuantity(quantity);

        ShippingAddressRequest shipping = new ShippingAddressRequest();
        shipping.setAddressLine1("Calle de prueba 123");
        shipping.setCity("CDMX");
        shipping.setState("CDMX");
        shipping.setPostalCode("01000");
        shipping.setCountry("Mexico");

        CardRequest card = new CardRequest();
        card.setHolderName("Test Buyer");
        card.setCardNumber("411111111111111" + cardNumberEndingIn);
        card.setExpiryMonth("12");
        card.setExpiryYear("2030");
        card.setCvv("123");

        CheckoutRequest request = new CheckoutRequest();
        request.setItems(List.of(item));
        request.setBuyerFirstName("Test");
        request.setBuyerLastName("Buyer");
        request.setBuyerEmail("buyer@test.com");
        request.setBuyerPhone("5551234567");
        request.setShippingAddress(shipping);
        request.setCard(card);
        return request;
    }

    @Test
    void approvedPaymentCreatesOrderWithCorrectTotalsAndDecrementsStock() {
        String token = adminToken();
        Category category = createCategory("Categoria-" + System.nanoTime());
        Long bookId = createBookWithStock(token, category, 200, 10, "approve");

        CheckoutRequest request = buildCheckout(bookId, 3, "2"); // termina en par -> aprobado

        var response = restTemplate.exchange(
                baseUrl("/api/checkout"), HttpMethod.POST, new HttpEntity<>(request),
                new ParameterizedTypeReference<ApiResponse<CheckoutResponse>>() {});

        CheckoutResponse result = response.getBody().getData();
        assertThat(result.isApproved()).isTrue();
        assertThat(result.getOrder().getSubtotal()).isEqualByComparingTo("600.00");
        assertThat(result.getOrder().getTotal()).isEqualByComparingTo("600.00");
        assertThat(result.getOrder().getStatus()).isEqualTo("PAGADO");

        int stockAfter = bookRepository.findById(bookId).orElseThrow().getStock();
        assertThat(stockAfter).isEqualTo(7); // 10 - 3
    }

    @Test
    void rejectedPaymentDoesNotDecrementStockButOrderIsRecorded() {
        String token = adminToken();
        Category category = createCategory("Categoria-" + System.nanoTime());
        Long bookId = createBookWithStock(token, category, 150, 5, "reject");

        CheckoutRequest request = buildCheckout(bookId, 2, "1"); // termina en impar -> rechazado

        var response = restTemplate.exchange(
                baseUrl("/api/checkout"), HttpMethod.POST, new HttpEntity<>(request),
                new ParameterizedTypeReference<ApiResponse<CheckoutResponse>>() {});

        CheckoutResponse result = response.getBody().getData();
        assertThat(result.isApproved()).isFalse();
        assertThat(result.getOrder().getStatus()).isEqualTo("CANCELADO");
        assertThat(result.getOrder().getPaymentStatus()).isEqualTo("RECHAZADO");

        int stockAfter = bookRepository.findById(bookId).orElseThrow().getStock();
        assertThat(stockAfter).isEqualTo(5); // sin cambios

        assertThat(orderRepository.findByFolio(result.getOrder().getFolio())).isPresent();
    }

    @Test
    void cannotCheckoutMoreThanAvailableStock() {
        String token = adminToken();
        Category category = createCategory("Categoria-" + System.nanoTime());
        Long bookId = createBookWithStock(token, category, 100, 2, "outofstock");

        CheckoutRequest request = buildCheckout(bookId, 5, "2");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/checkout"), HttpMethod.POST, new HttpEntity<>(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(bookRepository.findById(bookId).orElseThrow().getStock()).isEqualTo(2);
    }
}
