package com.acme.hr.controller;

import com.acme.hr.dto.LoginRequest;
import com.acme.hr.dto.LoginResponse;
import com.acme.hr.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Test
    void loginDelegatesAndReturnsToken() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        LoginRequest request = new LoginRequest("hr-user", "password");
        LoginResponse response = new LoginResponse("token", "Bearer", 3600, "hr-user", "HRManager");
        when(authService.login(request)).thenReturn(response);

        assertEquals(response, controller.login(request));
        verify(authService).login(request);
    }

    @Test
    void invalidCredentialsReturnUnauthorizedWithoutLeakingDetails() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        when(authService.login(any())).thenThrow(new BadCredentialsException("private detail"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> controller.login(new LoginRequest("user", "wrong")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid username or password.", exception.getReason());
    }
}
