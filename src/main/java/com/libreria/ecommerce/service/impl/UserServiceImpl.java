package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.dto.request.UserUpdateRequest;
import com.libreria.ecommerce.dto.response.UserResponse;
import com.libreria.ecommerce.entity.User;
import com.libreria.ecommerce.enums.RoleName;
import com.libreria.ecommerce.exception.ResourceNotFoundException;
import com.libreria.ecommerce.mapper.UserMapper;
import com.libreria.ecommerce.repository.UserRepository;
import com.libreria.ecommerce.security.SecurityUtils;
import com.libreria.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getMe() {
        return userMapper.toResponse(currentUser());
    }

    @Override
    @Transactional
    public UserResponse updateMe(UserUpdateRequest request) {
        User user = currentUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setUpdatedBy(user);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public Page<UserResponse> adminListCustomers(String q, Pageable pageable) {
        Page<User> page = (q == null || q.isBlank())
                ? userRepository.findByRole_Name(RoleName.USER, pageable)
                : userRepository.searchByRole(RoleName.USER, "%" + q.trim().toLowerCase() + "%", pageable);
        return page.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse adminDeactivate(Long userId) {
        User actor = SecurityUtils.getCurrentUserOrNull();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));
        user.setIsActive(false);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(actor);
        return userMapper.toResponse(userRepository.save(user));
    }

    private User currentUser() {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesión activa");
        }
        return user;
    }
}
