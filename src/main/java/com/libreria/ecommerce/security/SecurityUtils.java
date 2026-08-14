package com.libreria.ecommerce.security;

import com.libreria.ecommerce.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Acceso al usuario autenticado desde capas que no reciben el principal por inyección
 * (ej. {@code CheckoutService}, donde el comprador puede ser un invitado sin sesión).
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof User user)) {
            return null;
        }
        return user;
    }
}
