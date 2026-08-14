package com.libreria.ecommerce.mapper;

import com.libreria.ecommerce.dto.request.AddressRequest;
import com.libreria.ecommerce.dto.response.AddressResponse;
import com.libreria.ecommerce.entity.Address;
import com.libreria.ecommerce.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressRequest req, User owner) {
        Address address = new Address();
        address.setUser(owner);
        applyRequest(address, req);
        return address;
    }

    public void applyRequest(Address address, AddressRequest req) {
        address.setLabel(req.getLabel());
        address.setRecipientName(req.getRecipientName());
        address.setPhone(req.getPhone());
        address.setAddressLine1(req.getAddressLine1());
        address.setAddressLine2(req.getAddressLine2());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setPostalCode(req.getPostalCode());
        address.setCountry(req.getCountry());
        address.setIsDefault(req.isDefaultAddress());
    }

    public AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .defaultAddress(Boolean.TRUE.equals(address.getIsDefault()))
                .build();
    }
}
