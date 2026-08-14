package com.libreria.ecommerce.mapper;

import com.libreria.ecommerce.dto.response.AuthorResponse;
import com.libreria.ecommerce.entity.Author;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {
    public AuthorResponse toResponse(Author a) {
        return AuthorResponse.builder().id(a.getId()).name(a.getName()).bio(a.getBio()).build();
    }
}
