package com.cts.cts.service;

import com.cts.cts.dto.*;
import com.cts.cts.exception.NotFoundException;
import com.cts.cts.model.PasswordResetToken;
import com.cts.cts.model.Role;
import com.cts.cts.model.UserEntity;
import com.cts.cts.repository.PasswordResetTokenRepository;
import com.cts.cts.repository.UserRepository;
import com.cts.cts.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager,
                       PasswordResetTokenRepository tokenRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
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
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserEntity user = userRepository.findByEmail(request.email()).orElseThrow();
        return new AuthResponseDto(jwtService.generateToken(user), user.getRole().name());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordDto dto) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("MAIL_USERNAME no configurado. No se puede enviar el correo de recuperación.");
            return;
        }

        userRepository.findByEmail(dto.email().trim().toLowerCase()).ifPresent(user -> {
            tokenRepository.deleteByEmail(user.getEmail());

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setEmail(user.getEmail());
            resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
            resetToken.setUsed(false);
            tokenRepository.save(resetToken);

            sendResetEmail(user.getEmail(), resetToken.getToken());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordDto dto) {
        PasswordResetToken resetToken = tokenRepository.findByToken(dto.token())
                .orElseThrow(() -> new IllegalArgumentException(
                    "El enlace de recuperación es inválido o ya fue utilizado"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException(
                "Este enlace ya fue utilizado. Solicita uno nuevo si lo necesitas");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                "El enlace de recuperación ha expirado. Solicita uno nuevo");
        }

        UserEntity user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Contraseña restablecida exitosamente para {}", user.getEmail());
    }

    private void sendResetEmail(String email, String token) {
        try {
            String resetUrl = baseUrl + "/index.html?reset=" + token;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("CTS Consulting — Recuperación de contraseña");
            message.setText(
                "Has solicitado restablecer tu contraseña en CTS Consulting.\n\n" +
                "Haz clic en el siguiente enlace para crear una nueva contraseña:\n\n" +
                resetUrl + "\n\n" +
                "Este enlace es válido por 1 hora.\n\n" +
                "Si no solicitaste este cambio, puedes ignorar este correo.\n\n" +
                "Saludos,\nCTS Consulting"
            );
            mailSender.send(message);
            log.info("Correo de recuperación enviado a {}", email);
        } catch (Exception e) {
            log.error("Error al enviar correo de recuperación a {}: {}", email, e.getMessage(), e);
        }
    }
}