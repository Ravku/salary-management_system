package com.acme.hr.config;

import com.acme.hr.entity.UserAccount;
import com.acme.hr.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserAccountBootstrapTest {

    private final UserAccountRepository repository = mock(UserAccountRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void createsBothAccountsWithEncodedPasswordsAndCorrectRoles() throws Exception {
        when(repository.findByUsername(any())).thenReturn(Optional.empty());
        when(repository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserAccountBootstrap bootstrap = bootstrap("hr-user", "hr-password", "admin-user", "admin-password");

        bootstrap.run(new DefaultApplicationArguments(new String[0]));

        verify(repository, times(2)).save(any(UserAccount.class));
        var captor = org.mockito.ArgumentCaptor.forClass(UserAccount.class);
        verify(repository, times(2)).save(captor.capture());
        assertEquals("hr-user", captor.getAllValues().get(0).getUsername());
        assertEquals("HRManager", captor.getAllValues().get(0).getRole());
        assertTrue(passwordEncoder.matches("hr-password", captor.getAllValues().get(0).getPasswordHash()));
        assertTrue(captor.getAllValues().get(0).isActive());
        assertEquals("admin-user", captor.getAllValues().get(1).getUsername());
        assertEquals("ADMIN", captor.getAllValues().get(1).getRole());
        assertTrue(passwordEncoder.matches("admin-password", captor.getAllValues().get(1).getPasswordHash()));
    }

    @Test
    void preservesExistingAccounts() throws Exception {
        UserAccount existingHr = account("hr-user", "ROLE_HRMANAGER");
        UserAccount existingAdmin = account("admin-user", "ADMIN");
        when(repository.findByUsername("hr-user")).thenReturn(Optional.of(existingHr));
        when(repository.findByUsername("admin-user")).thenReturn(Optional.of(existingAdmin));
        UserAccountBootstrap bootstrap = bootstrap("hr-user", "new-password", "admin-user", "new-password");

        assertDoesNotThrow(() -> bootstrap.run(new DefaultApplicationArguments(new String[0])));

        verify(repository, never()).save(any());
    }

    @Test
    void rejectsMissingCredentials() {
        UserAccountBootstrap bootstrap = bootstrap("", "hr-password", "admin-user", "admin-password");

        assertThrows(IllegalStateException.class,
                () -> bootstrap.run(new DefaultApplicationArguments(new String[0])));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsSameBootstrapUsername() {
        UserAccountBootstrap bootstrap = bootstrap("same", "hr-password", "SAME", "admin-password");

        assertThrows(IllegalStateException.class,
                () -> bootstrap.run(new DefaultApplicationArguments(new String[0])));
        verifyNoInteractions(repository);
    }

    @Test
    void refusesToReuseUsernameWithDifferentRole() {
        when(repository.findByUsername("hr-user")).thenReturn(Optional.of(account("hr-user", "ADMIN")));
        UserAccountBootstrap bootstrap = bootstrap("hr-user", "hr-password", "admin-user", "admin-password");

        assertThrows(IllegalStateException.class,
                () -> bootstrap.run(new DefaultApplicationArguments(new String[0])));
        verify(repository, never()).save(any());
    }

    private UserAccountBootstrap bootstrap(
            String hrUsername, String hrPassword, String adminUsername, String adminPassword) {
        return new UserAccountBootstrap(repository, passwordEncoder,
                hrUsername, hrPassword, adminUsername, adminPassword);
    }

    private static UserAccount account(String username, String role) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setRole(role);
        return account;
    }
}
