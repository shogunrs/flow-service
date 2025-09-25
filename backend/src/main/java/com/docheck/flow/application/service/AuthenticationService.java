package com.docheck.flow.application.service;

import com.docheck.flow.api.dto.auth.AuthenticationRequest;
import com.docheck.flow.api.dto.auth.AuthenticationResponse;
import com.docheck.flow.api.dto.auth.RegisterRequest;
import com.docheck.flow.application.port.UserRepository;
import com.docheck.flow.config.jwt.JwtService;
import com.docheck.flow.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of("USER", "MANAGER")) // O primeiro usuário é o gerente da sua organização
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Salva o usuário para obter o ID gerado
        User savedUser = userRepository.save(user);

        // Define o usuário como a raiz de sua própria organização
        savedUser.setOrganizationId(savedUser.getId());
        savedUser.setCreatedBy(savedUser.getId());

        // Atualiza o usuário com os dados da organização
        User rootUser = userRepository.save(savedUser);

        var userDetails = new org.springframework.security.core.userdetails.User(rootUser.getEmail(), rootUser.getPasswordHash(), Set.of());
        var jwtToken = jwtService.generateToken(userDetails);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        var userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPasswordHash(), Set.of());
        var jwtToken = jwtService.generateToken(userDetails);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
