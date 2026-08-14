package com.libreria.ecommerce.service;

import com.libreria.ecommerce.dto.request.AuthorRequest;
import com.libreria.ecommerce.dto.response.AuthorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuthorService {
    List<AuthorResponse> listActive();
    Page<AuthorResponse> adminList(String q, Pageable pageable);
    AuthorResponse create(AuthorRequest request);
    AuthorResponse update(Long id, AuthorRequest request);
    void deactivate(Long id);
}
