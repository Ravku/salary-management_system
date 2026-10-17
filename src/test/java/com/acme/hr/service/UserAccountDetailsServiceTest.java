package com.acme.hr.service;

import com.acme.hr.entity.UserAccount;
import com.acme.hr.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserAccountDetailsServiceTest {

    @Test
    void loadsActiveAccountWithStoredRoleAndPasswordHash() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        UserAccount account = account("hr-user", "bcrypt-hash", "HRManager", true);
        when(repository.findByUsername("hr-user")).thenReturn(Optional.of(account));

        UserDetails details = new UserAccountDetailsService(repository).loadUserByUsername("hr-user");

        assertEquals("hr-user", details.getUsername());
        assertEquals("bcrypt-hash", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HRManager")));
    }

    @Test
    void loadsInactiveAccountAsDisabled() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        when(repository.findByUsername("disabled")).thenReturn(
                Optional.of(account("disabled", "hash", "HRManager", false)));

        UserDetails details = new UserAccountDetailsService(repository).loadUserByUsername("disabled");

        assertFalse(details.isEnabled());
    }

    @Test
    void acceptsRoleStoredWithRolePrefix() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        when(repository.findByUsername("admin")).thenReturn(
                Optional.of(account("admin", "hash", "ROLE_ADMIN", true)));

        UserDetails details = new UserAccountDetailsService(repository).loadUserByUsername("admin");

        assertTrue(details.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void normalizesCaseForHrManagerRole() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        when(repository.findByUsername("hr-user")).thenReturn(
                Optional.of(account("hr-user", "hash", "ROLE_HRMANAGER", true)));

        UserDetails details = new UserAccountDetailsService(repository).loadUserByUsername("hr-user");

        assertTrue(details.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HRManager")));
    }

    @Test
    void unknownUsernameThrowsUsernameNotFound() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        when(repository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> new UserAccountDetailsService(repository).loadUserByUsername("missing"));
    }

    @Test
    void accountWithoutRoleCannotAuthenticate() {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        when(repository.findByUsername("no-role")).thenReturn(
                Optional.of(account("no-role", "hash", " ", true)));

        assertThrows(UsernameNotFoundException.class,
                () -> new UserAccountDetailsService(repository).loadUserByUsername("no-role"));
    }

    private static UserAccount account(String username, String passwordHash, String role, boolean active) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordHash);
        account.setRole(role);
        account.setActive(active);
        return account;
    }
}
