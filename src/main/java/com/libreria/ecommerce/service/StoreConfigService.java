package com.libreria.ecommerce.service;

import com.libreria.ecommerce.dto.request.StoreConfigRequest;
import com.libreria.ecommerce.dto.response.StoreConfigResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StoreConfigService {
    StoreConfigResponse get();
    StoreConfigResponse update(StoreConfigRequest request);
    StoreConfigResponse updateLogo(MultipartFile file);
    StoreConfigResponse updateFavicon(MultipartFile file);
}
