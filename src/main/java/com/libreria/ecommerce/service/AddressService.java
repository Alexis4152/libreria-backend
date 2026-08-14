package com.libreria.ecommerce.service;

import com.libreria.ecommerce.dto.request.AddressRequest;
import com.libreria.ecommerce.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    List<AddressResponse> listMine();
    AddressResponse create(AddressRequest request);
    AddressResponse update(Long addressId, AddressRequest request);
    void delete(Long addressId);
}
