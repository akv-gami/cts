package com.cts.cts.service;

import com.cts.cts.dto.AuthRequestDto;
import com.cts.cts.dto.AuthResponseDto;
import com.cts.cts.dto.RegisterRequestDto;
import com.cts.cts.model.Role;
import com.cts.cts.model.UserEntity;
import com.cts.cts.repository.UserRepository;
import com.cts.cts.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        UserEntity user = new UserEntity();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return new AuthResponseDto(jwtService.generateToken(user), user.getRole().name());
    }

    public AuthResponseDto login(AuthRequestDto request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserEntity user = userRepository.findByEmail(request.email()).orElseThrow();
        return new AuthResponseDto(jwtService.generateToken(user), user.getRole().name());
    }
}