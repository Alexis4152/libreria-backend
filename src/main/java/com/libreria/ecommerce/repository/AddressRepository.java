package com.libreria.ecommerce.repository;

import com.libreria.ecommerce.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser_IdAndIsActiveTrue(Long userId);
}
