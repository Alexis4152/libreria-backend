package com.libreria.ecommerce.service.impl;

import com.libreria.ecommerce.dto.request.LoginRequest;
import com.libreria.ecommerce.dto.request.RegisterRequest;
import com.libreria.ecommerce.dto.response.LoginResponse;
import com.libreria.ecommerce.entity.Role;
import com.libreria.ecommerce.entity.User;
import com.libreria.ecommerce.enums.RoleName;
import com.libreria.ecommerce.exception.DuplicateResourceException;
import com.libreria.ecommerce.exception.ResourceNotFoundException;
import com.libreria.ecommerce.mapper.UserMapper;
import com.libreria.ecommerce.repository.RoleRepository;
import com.libreria.ecommerce.repository.UserRepository;
import com.libreria.ecommerce.security.JwtTokenProvider;
import com.libreria.ecommerce.security.SecurityUtils;
import com.libreria.ecommerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Ya existe una cuenta con ese correo");
        }
        // Registro público: el rol SIEMPRE es USER, nunca se acepta del cliente.
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("Rol USER no configurado"));

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(userRole)
                .build();
        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);
        return LoginResponse.builder().token(token).user(userMapper.toResponse(user)).build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = (User) authentication.getPrincipal();

        String token = jwtTokenProvider.generateToken(user);
        return LoginResponse.builder().token(token).user(userMapper.toResponse(user)).build();
    }

    @Override
    public User getCurrentUser() {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesión activa");
        }
        return user;
    }
}
