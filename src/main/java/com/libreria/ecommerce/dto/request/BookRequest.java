package com.libreria.ecommerce.dto.request;

import com.libreria.ecommerce.enums.CoverType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BookRequest {

    @NotBlank(message = "El SKU es obligatorio")
    private String sku;

    private String isbn;

    @NotBlank(message = "El título es obligatorio")
    private String title;

    private String subtitle;
    private String descriptionShort;
    private String descriptionLong;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    private Long publisherId;

    private List<Long> authorIds;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio no puede ser negativo")
    private BigDecimal price;

    @PositiveOrZero(message = "El precio promocional no puede ser negativo")
    private BigDecimal promoPrice;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    private Integer publicationYear;
    private Integer pageCount;
    private String language;
    private CoverType coverType;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private BigDecimal depthCm;
    private Integer weightGrams;
    private boolean featured;
    private boolean newRelease;
}
