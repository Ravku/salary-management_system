package com.acme.hr.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void rejectsShortJwtSigningSecret() {
        assertThrows(IllegalStateException.class, () -> securityConfig.jwtSecretKey("too-short"));
    }

    @Test
    void converterMapsJwtRolesToRoleAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("hr-user")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("roles", List.of("HRManager"))
                .build();

        var authentication = securityConfig.jwtAuthenticationConverter().convert(jwt);

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_HRManager")));
    }

    @Test
    void convertsAdminRoleWithoutGrantingHrRole() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("admin")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("roles", List.of("ADMIN"))
                .build();

        var authentication = securityConfig.jwtAuthenticationConverter().convert(jwt);

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        assertFalse(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_HRManager")));
    }

    @Test
    void corsAllowsConfiguredAngularOriginAndBearerHeaders() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource(
                "http://localhost:4200, https://hr.example.com");

        CorsConfiguration cors = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("OPTIONS", "/api/employees"));

        assertNotNull(cors);
        assertEquals(List.of("http://localhost:4200", "https://hr.example.com"), cors.getAllowedOrigins());
        assertTrue(cors.getAllowedMethods().containsAll(List.of("GET", "POST", "PUT", "OPTIONS")));
        assertTrue(cors.getAllowedHeaders().contains("Authorization"));
        assertTrue(cors.getAllowedHeaders().contains("Content-Type"));
        assertFalse(cors.getAllowCredentials());
    }

    @Test
    void corsRejectsWildcardOrigin() {
        assertThrows(IllegalStateException.class, () -> securityConfig.corsConfigurationSource("*"));
    }
}
