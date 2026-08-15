package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.dto.request.BookRequest;
import com.libreria.ecommerce.dto.response.BookDetailResponse;
import com.libreria.ecommerce.dto.response.BookImageResponse;
import com.libreria.ecommerce.dto.response.BookSummaryResponse;
import com.libreria.ecommerce.entity.Author;
import com.libreria.ecommerce.entity.Book;
import com.libreria.ecommerce.entity.BookImage;
import com.libreria.ecommerce.entity.Category;
import com.libreria.ecommerce.entity.Publisher;
import com.libreria.ecommerce.enums.BookStatus;
import com.libreria.ecommerce.exception.BusinessException;
import com.libreria.ecommerce.exception.DuplicateResourceException;
import com.libreria.ecommerce.exception.ResourceNotFoundException;
import com.libreria.ecommerce.mapper.BookMapper;
import com.libreria.ecommerce.repository.AuthorRepository;
import com.libreria.ecommerce.repository.BookRepository;
import com.libreria.ecommerce.repository.CategoryRepository;
import com.libreria.ecommerce.repository.PublisherRepository;
import com.libreria.ecommerce.security.SecurityUtils;
import com.libreria.ecommerce.service.BookService;
import com.libreria.ecommerce.service.BookSpecifications;
import com.libreria.ecommerce.service.FileStorageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final BookMapper bookMapper;
    private final FileStorageService fileStorageService;

    @PersistenceContext
    private EntityManager entityManager;

    // Lecturas anotadas @Transactional(readOnly = true): con spring.jpa.open-in-view=false
    // la sesión de Hibernate se cierra al terminar la consulta del repositorio, así que el
    // mapeo a DTO (que toca las colecciones LAZY authors/images) debe ocurrir dentro de la
    // misma transacción, no después.
    @Override
    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> search(String keyword, Long categoryId, Long authorId, Long publisherId,
                                             BigDecimal minPrice, BigDecimal maxPrice, Boolean inStockOnly,
                                             Boolean featuredOnly, Pageable pageable) {
        Specification<Book> spec = BookSpecifications.search(
                keyword, categoryId, authorId, publisherId, minPrice, maxPrice, inStockOnly, featuredOnly);
        return bookRepository.findAll(spec, pageable).map(bookMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookSummaryResponse> getFeatured() {
        return bookRepository.findTop8ByIsFeaturedTrueAndIsActiveTrueAndStatus(BookStatus.ACTIVE).stream()
                .map(bookMapper::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookDetailResponse getDetail(Long id) {
        Book book = bookRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado: " + id));
        List<BookSummaryResponse> related = bookRepository
                .findRelated(book.getCategory().getId(), book.getId(), PageRequest.of(0, 4))
                .map(bookMapper::toSummary)
                .getContent();
        return bookMapper.toDetail(book, related);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookSummaryResponse> adminList(String q, Long categoryId, Pageable pageable) {
        return bookRepository.findAll((root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("sku")), like)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable).map(bookMapper::toSummary);
    }

    @Override
    @Transactional
    public BookDetailResponse create(BookRequest request) {
        if (bookRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Ya existe un libro con el SKU: " + request.getSku());
        }
        Book book = new Book();
        applyRequest(book, request);
        book.setCreatedBy(SecurityUtils.getCurrentUserOrNull());
        book = bookRepository.save(book);
        return bookMapper.toDetail(book, List.of());
    }

    @Override
    @Transactional
    public BookDetailResponse update(Long id, BookRequest request) {
        Book book = findActive(id);
        if (!book.getSku().equals(request.getSku()) && bookRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Ya existe un libro con el SKU: " + request.getSku());
        }
        applyRequest(book, request);
        book.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        book = bookRepository.save(book);
        return bookMapper.toDetail(book, List.of());
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Book book = findActive(id);
        book.setIsActive(false);
        book.setDeletedAt(LocalDateTime.now());
        book.setDeletedBy(SecurityUtils.getCurrentUserOrNull());
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public BookDetailResponse adjustStock(Long id, Integer newStock) {
        if (newStock < 0) {
            throw new BusinessException("El stock no puede ser negativo");
        }
        Book book = findActive(id);
        book.setStock(newStock);
        book.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        book = bookRepository.save(book);
        return bookMapper.toDetail(book, List.of());
    }

    @Override
    @Transactional
    public BookImageResponse addImage(Long bookId, MultipartFile file, boolean primary) {
        Book book = findActive(bookId);
        String url = fileStorageService.store(file, "books");

        if (primary) {
            book.getImages().forEach(img -> img.setIsPrimary(false));
        }
        BookImage image = BookImage.builder()
                .book(book)
                .url(url)
                .isPrimary(primary || book.getImages().isEmpty())
                .sortOrder(book.getImages().size())
                .altText(book.getTitle())
                .build();
        // No usar bookRepository.save(book) aqui: book ya esta managed en esta transaccion,
        // asi que save() ejecuta entityManager.merge(book), y merge() cascada como MERGE
        // (no PERSIST) hacia la coleccion images. Para una BookImage transitoria, merge()
        // crea una copia interna y le asigna el id generado A LA COPIA, dejando esta
        // instancia "image" con id null para siempre. persist() directo evita esa copia.
        entityManager.persist(image);
        book.getImages().add(image);

        return BookImageResponse.builder()
                .id(image.getId()).url(bookMapper.absolute(image.getUrl())).primary(Boolean.TRUE.equals(image.getIsPrimary()))
                .sortOrder(image.getSortOrder()).altText(image.getAltText())
                .build();
    }

    @Override
    @Transactional
    public void deleteImage(Long bookId, Long imageId) {
        Book book = findActive(bookId);
        boolean removed = book.getImages().removeIf(img -> img.getId().equals(imageId));
        if (!removed) {
            throw new ResourceNotFoundException("Imagen no encontrada: " + imageId);
        }
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public BookImageResponse setPrimaryImage(Long bookId, Long imageId) {
        Book book = findActive(bookId);
        BookImage target = book.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada: " + imageId));
        book.getImages().forEach(img -> img.setIsPrimary(img.getId().equals(imageId)));
        bookRepository.save(book);

        return BookImageResponse.builder()
                .id(target.getId()).url(bookMapper.absolute(target.getUrl())).primary(true)
                .sortOrder(target.getSortOrder()).altText(target.getAltText())
                .build();
    }

    private void applyRequest(Book book, BookRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + request.getCategoryId()));
        Publisher publisher = request.getPublisherId() != null
                ? publisherRepository.findById(request.getPublisherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Editorial no encontrada: " + request.getPublisherId()))
                : null;
        // Lista mutable siempre: Hibernate necesita poder hacer .clear() sobre la colección
        // persistente al reconciliar cambios (List.of() es inmutable y revienta ese merge).
        List<Author> authors = request.getAuthorIds() != null
                ? new ArrayList<>(authorRepository.findAllById(request.getAuthorIds()))
                : new ArrayList<>();

        book.setSku(request.getSku());
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setSubtitle(request.getSubtitle());
        book.setDescriptionShort(request.getDescriptionShort());
        book.setDescriptionLong(request.getDescriptionLong());
        book.setCategory(category);
        book.setPublisher(publisher);
        book.setAuthors(authors);
        book.setPrice(request.getPrice());
        book.setPromoPrice(request.getPromoPrice());
        book.setStock(request.getStock());
        book.setPublicationYear(request.getPublicationYear());
        book.setPageCount(request.getPageCount());
        book.setLanguage(request.getLanguage());
        book.setCoverType(request.getCoverType());
        book.setWidthCm(request.getWidthCm());
        book.setHeightCm(request.getHeightCm());
        book.setDepthCm(request.getDepthCm());
        book.setWeightGrams(request.getWeightGrams());
        book.setIsFeatured(request.isFeatured());
        book.setIsNew(request.isNewRelease());
        if (book.getStatus() == null) {
            book.setStatus(BookStatus.ACTIVE);
        }
    }

    private Book findActive(Long id) {
        Book book = bookRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado: " + id));
        return book;
    }
}
