package com.libreria.ecommerce.service;

import com.libreria.ecommerce.dto.request.PublisherRequest;
import com.libreria.ecommerce.dto.response.PublisherResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PublisherService {
    List<PublisherResponse> listActive();
    Page<PublisherResponse> adminList(String q, Pageable pageable);
    PublisherResponse create(PublisherRequest request);
    PublisherResponse update(Long id, PublisherRequest request);
    void deactivate(Long id);
}
