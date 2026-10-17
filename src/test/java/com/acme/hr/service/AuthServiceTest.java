package com.acme.hr.service;

import com.acme.hr.dto.LoginRequest;
import com.acme.hr.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Test
    void authenticatesCredentialsAndReturnsBearerToken() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        when(authenticationManager.authenticate(any())).thenReturn(
                UsernamePasswordAuthenticationToken.authenticated(
                        "hr-user", null, List.of(new SimpleGrantedAuthority("ROLE_HRManager"))));
        Jwt encodedJwt = mock(Jwt.class);
        when(encodedJwt.getTokenValue()).thenReturn("signed.jwt.value");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(encodedJwt);
        AuthService service = new AuthService(authenticationManager, jwtEncoder, Duration.ofMinutes(30));

        LoginResponse result = service.login(new LoginRequest("hr-user", "password"));

        assertEquals("signed.jwt.value", result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(1800, result.expiresIn());
        assertEquals("hr-user", result.username());
        assertEquals("HRManager", result.role());
        verify(authenticationManager).authenticate(argThat(authentication ->
                authentication instanceof UsernamePasswordAuthenticationToken
                        && authentication.getName().equals("hr-user")
                        && authentication.getCredentials().equals("password")));
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void rejectsMissingCredentialsBeforeAuthentication() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        AuthService service = new AuthService(authenticationManager, jwtEncoder, Duration.ofHours(1));

        assertThrows(IllegalArgumentException.class, () -> service.login(null));
        assertThrows(IllegalArgumentException.class,
                () -> service.login(new LoginRequest(" ", "password")));
        assertThrows(IllegalArgumentException.class,
                () -> service.login(new LoginRequest("user", "")));
        verifyNoInteractions(authenticationManager, jwtEncoder);
    }

    @Test
    void rejectsNonPositiveTokenLifetime() {
        assertThrows(IllegalArgumentException.class,
                () -> new AuthService(mock(AuthenticationManager.class), mock(JwtEncoder.class), Duration.ZERO));
    }
}
