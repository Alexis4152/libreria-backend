package com.libreria.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** Fila única de configuración/branding de la tienda, consumida dinámicamente por el frontend. */
@Entity
@Table(name = "store_config")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StoreConfig {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_name", nullable = false, length = 150)
    private String storeName;

    @Column(name = "legal_name", length = 150)
    private String legalName;

    @Column(length = 20)
    private String rfc;

    @Column(length = 255)
    private String address;

    @Column(length = 30)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;

    @Column(name = "primary_color", nullable = false, length = 10)
    private String primaryColor;

    @Column(name = "secondary_color", nullable = false, length = 10)
    private String secondaryColor;

    @Column(name = "welcome_message", length = 500)
    private String welcomeMessage;

    @Column(name = "ticket_message", length = 500)
    private String ticketMessage;

    // Sin columnDefinition="jsonb" explícito (específico de Postgres) para que el mismo
    // mapeo funcione también contra H2 en pruebas; el dialecto de cada entorno decide el
    // tipo de columna real a partir de @JdbcTypeCode.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links")
    private String socialLinks;

    @Column(name = "shipping_info", length = 500)
    private String shippingInfo;

    @Column(name = "footer_text", length = 500)
    private String footerText;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id")
    private User updatedBy;
}
