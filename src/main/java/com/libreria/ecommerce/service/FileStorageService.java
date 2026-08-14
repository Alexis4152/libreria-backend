package com.libreria.ecommerce.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /** Guarda el archivo bajo {@code subdir} y devuelve la URL pública ({@code /uploads/...}). */
    String store(MultipartFile file, String subdir);
}
