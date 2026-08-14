package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.dto.request.AddressRequest;
import com.libreria.ecommerce.dto.response.AddressResponse;
import com.libreria.ecommerce.entity.Address;
import com.libreria.ecommerce.entity.User;
import com.libreria.ecommerce.exception.ResourceNotFoundException;
import com.libreria.ecommerce.mapper.AddressMapper;
import com.libreria.ecommerce.repository.AddressRepository;
import com.libreria.ecommerce.security.SecurityUtils;
import com.libreria.ecommerce.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Override
    public List<AddressResponse> listMine() {
        return addressRepository.findByUser_IdAndIsActiveTrue(currentUser().getId()).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse create(AddressRequest request) {
        User owner = currentUser();
        if (request.isDefaultAddress()) {
            clearPreviousDefault(owner.getId());
        }
        Address address = addressMapper.toEntity(request, owner);
        address.setCreatedBy(owner);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse update(Long addressId, AddressRequest request) {
        User owner = currentUser();
        Address address = findOwned(addressId, owner.getId());
        if (request.isDefaultAddress() && !Boolean.TRUE.equals(address.getIsDefault())) {
            clearPreviousDefault(owner.getId());
        }
        addressMapper.applyRequest(address, request);
        address.setUpdatedBy(owner);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void delete(Long addressId) {
        User owner = currentUser();
        Address address = findOwned(addressId, owner.getId());
        address.setIsActive(false);
        address.setDeletedAt(LocalDateTime.now());
        address.setDeletedBy(owner);
        addressRepository.save(address);
    }

    private Address findOwned(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada: " + addressId));
        if (!address.getUser().getId().equals(userId) || !Boolean.TRUE.equals(address.getIsActive())) {
            throw new ResourceNotFoundException("Dirección no encontrada: " + addressId);
        }
        return address;
    }

    private void clearPreviousDefault(Long userId) {
        addressRepository.findByUser_IdAndIsActiveTrue(userId).forEach(a -> {
            if (Boolean.TRUE.equals(a.getIsDefault())) {
                a.setIsDefault(false);
                addressRepository.save(a);
            }
        });
    }

    private User currentUser() {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesión activa");
        }
        return user;
    }
}
