package com.libreria.ecommerce;

import com.libreria.ecommerce.dto.request.BookRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Un USER jamás debe poder ejecutar operaciones exclusivas de ADMIN, ni manipulando la
 * petición HTTP directamente (sección 6/45 del prompt maestro): la autorización se valida
 * en Backend (SecurityConfig + hasRole), no solo ocultando botones en Frontend.
 */
class SecurityRoleTest extends AbstractIntegrationTest {

    @Test
    void userCannotCreateBooks() {
        String email = "user-" + System.nanoTime() + "@test.com";
        var userRole = roleRepository.findByName(com.libreria.ecommerce.enums.RoleName.USER).orElseThrow();
        var user = com.libreria.ecommerce.entity.User.builder()
                .email(email).passwordHash(passwordEncoder.encode("Password123"))
                .firstName("U").lastName("Ser").role(userRole).build();
        userRepository.save(user);
        String token = loginAndGetToken(email, "Password123");

        BookRequest req = new BookRequest();
        req.setSku("SKU-FORBIDDEN-" + System.nanoTime());
        req.setTitle("Intento no autorizado");
        req.setCategoryId(1L);
        req.setPrice(new BigDecimal("100"));
        req.setStock(1);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/admin/books"), HttpMethod.POST,
                new HttpEntity<>(req, authHeaders(token)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminCanCreateBooks() {
        String token = adminToken();
        var category = createCategory("Categoria-" + System.nanoTime());

        BookRequest req = new BookRequest();
        req.setSku("SKU-OK-" + System.nanoTime());
        req.setTitle("Libro permitido");
        req.setCategoryId(category.getId());
        req.setPrice(new BigDecimal("150"));
        req.setStock(5);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl("/api/admin/books"), HttpMethod.POST,
                new HttpEntity<>(req, authHeaders(token)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void adminEndpointsRejectRequestsWithoutToken() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl("/api/admin/books"), String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.UNAUTHORIZED);
    }
}
