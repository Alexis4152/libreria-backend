package com.libreria.ecommerce;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.request.*;
import com.libreria.ecommerce.dto.response.BookDetailResponse;
import com.libreria.ecommerce.dto.response.CheckoutResponse;
import com.libreria.ecommerce.dto.response.OrderDetailResponse;
import com.libreria.ecommerce.entity.Category;
import com.libreria.ecommerce.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest extends AbstractIntegrationTest {

    private OrderDetailResponse createPaidOrder(String adminToken) {
        Category category = createCategory("Categoria-" + System.nanoTime());
        BookRequest bookReq = new BookRequest();
        bookReq.setSku("SKU-ORD-" + System.nanoTime());
        bookReq.setTitle("Libro pedido");
        bookReq.setCategoryId(category.getId());
        bookReq.setPrice(new BigDecimal("100"));
        bookReq.setStock(10);
        var bookResp = restTemplate.exchange(baseUrl("/api/admin/books"), HttpMethod.POST,
                new HttpEntity<>(bookReq, authHeaders(adminToken)),
                new ParameterizedTypeReference<ApiResponse<BookDetailResponse>>() {});
        Long bookId = bookResp.getBody().getData().getId();

        CheckoutItemRequest item = new CheckoutItemRequest();
        item.setBookId(bookId);
        item.setQuantity(1);
        ShippingAddressRequest shipping = new ShippingAddressRequest();
        shipping.setAddressLine1("Calle 1");
        shipping.setCity("CDMX");
        shipping.setState("CDMX");
        shipping.setPostalCode("01000");
        shipping.setCountry("Mexico");
        CardRequest card = new CardRequest();
        card.setHolderName("Buyer");
        card.setCardNumber("4111111111111112");
        card.setExpiryMonth("12");
        card.setExpiryYear("2030");
        card.setCvv("123");
        CheckoutRequest checkoutReq = new CheckoutRequest();
        checkoutReq.setItems(List.of(item));
        checkoutReq.setBuyerFirstName("Test");
        checkoutReq.setBuyerLastName("Buyer");
        checkoutReq.setBuyerEmail("buyer@test.com");
        checkoutReq.setBuyerPhone("5551234567");
        checkoutReq.setShippingAddress(shipping);
        checkoutReq.setCard(card);

        var checkoutResp = restTemplate.exchange(baseUrl("/api/checkout"), HttpMethod.POST,
                new HttpEntity<>(checkoutReq), new ParameterizedTypeReference<ApiResponse<CheckoutResponse>>() {});
        return checkoutResp.getBody().getData().getOrder();
    }

    @Test
    void adminCanAdvanceOrderThroughValidTransitions() {
        String token = adminToken();
        OrderDetailResponse order = createPaidOrder(token);
        assertThat(order.getStatus()).isEqualTo("PAGADO");

        var prep = updateStatus(token, order.getId(), OrderStatus.PREPARANDO);
        assertThat(prep.getStatusCode()).isEqualTo(HttpStatus.OK);

        var shipped = updateStatus(token, order.getId(), OrderStatus.ENVIADO);
        assertThat(shipped.getStatusCode()).isEqualTo(HttpStatus.OK);

        var delivered = updateStatus(token, order.getId(), OrderStatus.ENTREGADO);
        assertThat(delivered.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void cannotSkipOrReviveOrderStatus() {
        String token = adminToken();
        OrderDetailResponse order = createPaidOrder(token);

        // PAGADO -> ENTREGADO directo no es una transición válida.
        var invalid = updateStatus(token, order.getId(), OrderStatus.ENTREGADO);
        assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private org.springframework.http.ResponseEntity<String> updateStatus(String token, Long orderId, OrderStatus status) {
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest();
        req.setStatus(status);
        return restTemplate.exchange(baseUrl("/api/admin/orders/" + orderId + "/status"), HttpMethod.PATCH,
                new HttpEntity<>(req, authHeaders(token)), String.class);
    }
}
