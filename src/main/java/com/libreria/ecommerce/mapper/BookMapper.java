package com.libreria.ecommerce.mapper;

import com.libreria.ecommerce.dto.response.*;
import com.libreria.ecommerce.entity.Book;
import com.libreria.ecommerce.entity.BookImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookMapper {

    public static final String DEFAULT_COVER_URL = "/uploads/books/placeholder.svg";

    private final CategoryMapper categoryMapper;
    private final PublisherMapper publisherMapper;
    private final AuthorMapper authorMapper;

    public BookSummaryResponse toSummary(Book book) {
        return BookSummaryResponse.builder()
                .id(book.getId())
                .sku(book.getSku())
                .title(book.getTitle())
                .subtitle(book.getSubtitle())
                .authorNames(joinAuthorNames(book))
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .publisherName(book.getPublisher() != null ? book.getPublisher().getName() : null)
                .price(book.getPrice())
                .promoPrice(book.getPromoPrice())
                .stock(book.getStock())
                .coverImageUrl(coverImageUrl(book))
                .featured(Boolean.TRUE.equals(book.getIsFeatured()))
                .newRelease(Boolean.TRUE.equals(book.getIsNew()))
                .build();
    }

    public BookDetailResponse toDetail(Book book, List<BookSummaryResponse> relatedBooks) {
        List<BookImageResponse> images = book.getImages().stream()
                .sorted(Comparator.comparing(BookImage::getSortOrder))
                .map(img -> BookImageResponse.builder()
                        .id(img.getId())
                        .url(img.getUrl())
                        .primary(Boolean.TRUE.equals(img.getIsPrimary()))
                        .sortOrder(img.getSortOrder())
                        .altText(img.getAltText())
                        .build())
                .toList();

        List<AuthorResponse> authors = book.getAuthors().stream().map(authorMapper::toResponse).toList();

        return BookDetailResponse.builder()
                .id(book.getId())
                .sku(book.getSku())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .subtitle(book.getSubtitle())
                .descriptionShort(book.getDescriptionShort())
                .descriptionLong(book.getDescriptionLong())
                .category(book.getCategory() != null ? categoryMapper.toResponse(book.getCategory()) : null)
                .publisher(book.getPublisher() != null ? publisherMapper.toResponse(book.getPublisher()) : null)
                .authors(authors)
                .price(book.getPrice())
                .promoPrice(book.getPromoPrice())
                .stock(book.getStock())
                .publicationYear(book.getPublicationYear())
                .pageCount(book.getPageCount())
                .language(book.getLanguage())
                .coverType(book.getCoverType() != null ? book.getCoverType().name() : null)
                .widthCm(book.getWidthCm())
                .heightCm(book.getHeightCm())
                .depthCm(book.getDepthCm())
                .weightGrams(book.getWeightGrams())
                .featured(Boolean.TRUE.equals(book.getIsFeatured()))
                .newRelease(Boolean.TRUE.equals(book.getIsNew()))
                .status(book.getStatus() != null ? book.getStatus().name() : null)
                .images(images)
                .relatedBooks(relatedBooks)
                .build();
    }

    private String joinAuthorNames(Book book) {
        if (book.getAuthors() == null || book.getAuthors().isEmpty()) {
            return null;
        }
        return book.getAuthors().stream().map(a -> a.getName()).reduce((a, b) -> a + ", " + b).orElse(null);
    }

    private String coverImageUrl(Book book) {
        return book.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(BookImage::getUrl)
                .findFirst()
                .orElseGet(() -> book.getImages().stream()
                        .findFirst()
                        .map(BookImage::getUrl)
                        .orElse(DEFAULT_COVER_URL));
    }
}
