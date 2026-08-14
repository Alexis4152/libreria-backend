package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.dto.request.PublisherRequest;
import com.libreria.ecommerce.dto.response.PublisherResponse;
import com.libreria.ecommerce.entity.Publisher;
import com.libreria.ecommerce.exception.ResourceNotFoundException;
import com.libreria.ecommerce.mapper.PublisherMapper;
import com.libreria.ecommerce.repository.PublisherRepository;
import com.libreria.ecommerce.security.SecurityUtils;
import com.libreria.ecommerce.service.PublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    @Override
    public List<PublisherResponse> listActive() {
        return publisherRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(publisherMapper::toResponse).toList();
    }

    @Override
    public Page<PublisherResponse> adminList(String q, Pageable pageable) {
        var page = (q == null || q.isBlank())
                ? publisherRepository.findByIsActiveTrue(pageable)
                : publisherRepository.findByIsActiveTrueAndNameContainingIgnoreCase(q.trim(), pageable);
        return page.map(publisherMapper::toResponse);
    }

    @Override
    @Transactional
    public PublisherResponse create(PublisherRequest request) {
        Publisher publisher = new Publisher();
        publisher.setName(request.getName());
        publisher.setCreatedBy(SecurityUtils.getCurrentUserOrNull());
        return publisherMapper.toResponse(publisherRepository.save(publisher));
    }

    @Override
    @Transactional
    public PublisherResponse update(Long id, PublisherRequest request) {
        Publisher publisher = findActive(id);
        publisher.setName(request.getName());
        publisher.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return publisherMapper.toResponse(publisherRepository.save(publisher));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Publisher publisher = findActive(id);
        publisher.setIsActive(false);
        publisher.setDeletedAt(LocalDateTime.now());
        publisher.setDeletedBy(SecurityUtils.getCurrentUserOrNull());
        publisherRepository.save(publisher);
    }

    private Publisher findActive(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Editorial no encontrada: " + id));
        if (!Boolean.TRUE.equals(publisher.getIsActive())) {
            throw new ResourceNotFoundException("Editorial no encontrada: " + id);
        }
        return publisher;
    }
}
