package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.dto.request.AuthorRequest;
import com.libreria.ecommerce.dto.response.AuthorResponse;
import com.libreria.ecommerce.entity.Author;
import com.libreria.ecommerce.exception.ResourceNotFoundException;
import com.libreria.ecommerce.mapper.AuthorMapper;
import com.libreria.ecommerce.repository.AuthorRepository;
import com.libreria.ecommerce.security.SecurityUtils;
import com.libreria.ecommerce.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Override
    public List<AuthorResponse> listActive() {
        return authorRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(authorMapper::toResponse).toList();
    }

    @Override
    public Page<AuthorResponse> adminList(String q, Pageable pageable) {
        var page = (q == null || q.isBlank())
                ? authorRepository.findByIsActiveTrue(pageable)
                : authorRepository.findByIsActiveTrueAndNameContainingIgnoreCase(q.trim(), pageable);
        return page.map(authorMapper::toResponse);
    }

    @Override
    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        Author author = new Author();
        author.setName(request.getName());
        author.setBio(request.getBio());
        author.setCreatedBy(SecurityUtils.getCurrentUserOrNull());
        return authorMapper.toResponse(authorRepository.save(author));
    }

    @Override
    @Transactional
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = findActive(id);
        author.setName(request.getName());
        author.setBio(request.getBio());
        author.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return authorMapper.toResponse(authorRepository.save(author));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Author author = findActive(id);
        author.setIsActive(false);
        author.setDeletedAt(LocalDateTime.now());
        author.setDeletedBy(SecurityUtils.getCurrentUserOrNull());
        authorRepository.save(author);
    }

    private Author findActive(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado: " + id));
        if (!Boolean.TRUE.equals(author.getIsActive())) {
            throw new ResourceNotFoundException("Autor no encontrado: " + id);
        }
        return author;
    }
}
