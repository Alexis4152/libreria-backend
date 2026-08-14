package com.libreria.ecommerce.service;

import com.libreria.ecommerce.dto.request.LoginRequest;
import com.libreria.ecommerce.dto.request.RegisterRequest;
import com.libreria.ecommerce.dto.response.LoginResponse;
import com.libreria.ecommerce.entity.User;

public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    User getCurrentUser();
}
