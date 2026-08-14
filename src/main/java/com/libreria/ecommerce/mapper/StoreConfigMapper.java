package com.libreria.ecommerce.mapper;

import com.libreria.ecommerce.dto.response.StoreConfigResponse;
import com.libreria.ecommerce.entity.StoreConfig;
import org.springframework.stereotype.Component;

@Component
public class StoreConfigMapper {
    public StoreConfigResponse toResponse(StoreConfig c) {
        return StoreConfigResponse.builder()
                .storeName(c.getStoreName())
                .legalName(c.getLegalName())
                .rfc(c.getRfc())
                .address(c.getAddress())
                .phone(c.getPhone())
                .email(c.getEmail())
                .logoUrl(c.getLogoUrl())
                .faviconUrl(c.getFaviconUrl())
                .primaryColor(c.getPrimaryColor())
                .secondaryColor(c.getSecondaryColor())
                .welcomeMessage(c.getWelcomeMessage())
                .ticketMessage(c.getTicketMessage())
                .socialLinks(c.getSocialLinks())
                .shippingInfo(c.getShippingInfo())
                .footerText(c.getFooterText())
                .build();
    }
}
