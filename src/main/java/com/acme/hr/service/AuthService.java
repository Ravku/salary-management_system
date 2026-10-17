package com.acme.hr.service;

import com.acme.hr.dto.LoginRequest;
import com.acme.hr.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final Duration tokenLifetime;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            @Value("${app.security.jwt.expiration:PT1H}") Duration tokenLifetime) {
        if (tokenLifetime.isZero() || tokenLifetime.isNegative()) {
            throw new IllegalArgumentException("JWT expiration must be a positive duration.");
        }
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.tokenLifetime = tokenLifetime;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("username and password are required.");
        }

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.username().trim(), request.password()));
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Authenticated account has no role."));

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(tokenLifetime);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("salary-management")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(authentication.getName())
                .claim("roles", List.of(role))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", tokenLifetime.toSeconds(), authentication.getName(), role);
    }
}
