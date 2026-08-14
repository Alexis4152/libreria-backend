package com.libreria.ecommerce.service;

import com.libreria.ecommerce.dto.request.UserUpdateRequest;
import com.libreria.ecommerce.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse getMe();
    UserResponse updateMe(UserUpdateRequest request);
    Page<UserResponse> adminListCustomers(String q, Pageable pageable);
    UserResponse adminDeactivate(Long userId);
}
