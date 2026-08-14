package com.libreria.ecommerce.mapper;

import com.libreria.ecommerce.dto.response.PublisherResponse;
import com.libreria.ecommerce.entity.Publisher;
import org.springframework.stereotype.Component;

@Component
public class PublisherMapper {
    public PublisherResponse toResponse(Publisher p) {
        return PublisherResponse.builder().id(p.getId()).name(p.getName()).build();
    }
}
