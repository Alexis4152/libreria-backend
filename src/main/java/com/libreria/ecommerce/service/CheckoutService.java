package com.libreria.ecommerce.service;

import com.libreria.ecommerce.dto.request.CheckoutRequest;
import com.libreria.ecommerce.dto.response.CheckoutResponse;

public interface CheckoutService {
    CheckoutResponse checkout(CheckoutRequest request);
}
