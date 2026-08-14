package com.libreria.ecommerce;

import com.libreria.ecommerce.dto.ApiResponse;
import com.libreria.ecommerce.dto.request.LoginRequest;
import com.libreria.ecommerce.dto.response.LoginResponse;
import com.libreria.ecommerce.entity.Category;
import com.libreria.ecommerce.entity.Role;
import com.libreria.ecommerce.entity.User;
import com.libreria.ecommerce.enums.RoleName;
import com.libreria.ecommerce.repository.CategoryRepository;
import com.libreria.ecommerce.repository.RoleRepository;
import com.libreria.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

/** Base de las pruebas de integración: app real (puerto aleatorio) + H2, con helpers
 * para crear usuarios/categorías/libros de prueba y autenticarse contra la API real. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void ensureRoles() {
        // data.sql ya las siembra, pero por si un test corre en un contexto sin recargar.
        if (roleRepository.findByName(RoleName.ADMIN).isEmpty()) {
            roleRepository.save(Role.builder().name(RoleName.ADMIN).build());
        }
        if (roleRepository.findByName(RoleName.USER).isEmpty()) {
            roleRepository.save(Role.builder().name(RoleName.USER).build());
        }
    }

    protected String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    protected User createAdminUser(String email, String rawPassword) {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstName("Admin")
                .lastName("Test")
                .role(adminRole)
                .build();
        return userRepository.save(user);
    }

    protected String loginAndGetToken(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        ResponseEntity<ApiResponse<LoginResponse>> response = restTemplate.exchange(
                baseUrl("/api/auth/login"),
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {}
        );
        return response.getBody().getData().getToken();
    }

    protected String adminToken() {
        String email = "admin-" + UUID.randomUUID() + "@test.com";
        createAdminUser(email, "Admin123!");
        return loginAndGetToken(email, "Admin123!");
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    protected Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(name.toLowerCase().replace(" ", "-") + "-" + UUID.randomUUID().toString().substring(0, 6));
        return categoryRepository.save(category);
    }
}
