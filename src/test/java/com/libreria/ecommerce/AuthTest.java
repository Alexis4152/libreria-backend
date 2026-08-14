package com.libreria.ecommerce;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.request.LoginRequest;
import com.libreria.ecommerce.dto.request.RegisterRequest;
import com.libreria.ecommerce.dto.response.LoginResponse;
import com.libreria.ecommerce.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AuthTest extends AbstractIntegrationTest {

    @Test
    void registerCreatesUserRoleNeverAdmin() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("nuevo-" + System.nanoTime() + "@test.com");
        req.setPassword("Password123");
        req.setFirstName("Nuevo");
        req.setLastName("Cliente");

        ResponseEntity<ApiResponse<LoginResponse>> response = restTemplate.exchange(
                baseUrl("/api/auth/register"), HttpMethod.POST, new HttpEntity<>(req),
                new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getData().getUser().getRole()).isEqualTo("USER");

        User saved = userRepository.findByEmail(req.getEmail()).orElseThrow();
        assertThat(saved.getRole().getName().name()).isEqualTo("USER");
        // La contraseña nunca se guarda en texto plano.
        assertThat(saved.getPasswordHash()).isNotEqualTo(req.getPassword());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("duplicado-" + System.nanoTime() + "@test.com");
        req.setPassword("Password123");
        req.setFirstName("A");
        req.setLastName("B");

        restTemplate.postForEntity(baseUrl("/api/auth/register"), req, ApiResponse.class);
        ResponseEntity<ApiResponse> second = restTemplate.postForEntity(
                baseUrl("/api/auth/register"), req, ApiResponse.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void loginSucceedsWithValidCredentials() {
        String email = "login-ok-" + System.nanoTime() + "@test.com";
        createAdminUser(email, "Secret123!");

        String token = loginAndGetToken(email, "Secret123!");

        assertThat(token).isNotBlank();
    }

    @Test
    void loginFailsWithWrongPassword() {
        String email = "login-fail-" + System.nanoTime() + "@test.com";
        createAdminUser(email, "Correct123!");

        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword("WrongPassword");

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                baseUrl("/api/auth/login"), req, ApiResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
