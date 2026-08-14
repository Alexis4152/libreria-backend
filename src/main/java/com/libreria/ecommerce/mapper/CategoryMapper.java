package com.libreria.ecommerce.mapper;

import com.libreria.ecommerce.dto.response.CategoryResponse;
import com.libreria.ecommerce.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId()).name(c.getName()).slug(c.getSlug()).description(c.getDescription())
                .build();
    }
}
