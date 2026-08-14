package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.dto.request.StoreConfigRequest;
import com.libreria.ecommerce.dto.response.StoreConfigResponse;
import com.libreria.ecommerce.entity.StoreConfig;
import com.libreria.ecommerce.mapper.StoreConfigMapper;
import com.libreria.ecommerce.repository.StoreConfigRepository;
import com.libreria.ecommerce.security.SecurityUtils;
import com.libreria.ecommerce.service.FileStorageService;
import com.libreria.ecommerce.service.StoreConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StoreConfigServiceImpl implements StoreConfigService {

    private final StoreConfigRepository storeConfigRepository;
    private final StoreConfigMapper storeConfigMapper;
    private final FileStorageService fileStorageService;

    @Override
    public StoreConfigResponse get() {
        return storeConfigMapper.toResponse(current());
    }

    @Override
    @Transactional
    public StoreConfigResponse update(StoreConfigRequest request) {
        StoreConfig config = current();
        config.setStoreName(request.getStoreName());
        config.setLegalName(request.getLegalName());
        config.setRfc(request.getRfc());
        config.setAddress(request.getAddress());
        config.setPhone(request.getPhone());
        config.setEmail(request.getEmail());
        config.setPrimaryColor(request.getPrimaryColor());
        config.setSecondaryColor(request.getSecondaryColor());
        config.setWelcomeMessage(request.getWelcomeMessage());
        config.setTicketMessage(request.getTicketMessage());
        config.setSocialLinks(request.getSocialLinks());
        config.setShippingInfo(request.getShippingInfo());
        config.setFooterText(request.getFooterText());
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return storeConfigMapper.toResponse(storeConfigRepository.save(config));
    }

    @Override
    @Transactional
    public StoreConfigResponse updateLogo(MultipartFile file) {
        StoreConfig config = current();
        config.setLogoUrl(fileStorageService.store(file, "store"));
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return storeConfigMapper.toResponse(storeConfigRepository.save(config));
    }

    @Override
    @Transactional
    public StoreConfigResponse updateFavicon(MultipartFile file) {
        StoreConfig config = current();
        config.setFaviconUrl(fileStorageService.store(file, "store"));
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return storeConfigMapper.toResponse(storeConfigRepository.save(config));
    }

    private StoreConfig current() {
        // Fila única (ver 02_seed.sql); si por alguna razón no existiera, se crea una por defecto
        // en vez de fallar, para que la tienda nunca quede sin configuración que mostrar.
        return storeConfigRepository.findAll().stream().findFirst()
                .orElseGet(() -> storeConfigRepository.save(StoreConfig.builder()
                        .storeName("Mi Librería Online")
                        .primaryColor("#155DEA")
                        .secondaryColor("#0F172A")
                        .updatedAt(LocalDateTime.now())
                        .build()));
    }
}
