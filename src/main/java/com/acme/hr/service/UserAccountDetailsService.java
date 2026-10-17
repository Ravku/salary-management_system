package com.acme.hr.service;

import com.acme.hr.entity.UserAccount;
import com.acme.hr.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserAccountDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User account was not found."));

        String role = account.getRole();
        if (role == null || role.isBlank()) {
            throw new UsernameNotFoundException("User account has no assigned role.");
        }
        role = canonicalRole(role);

        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .roles(role)
                .disabled(!account.isActive())
                .build();
    }

    private String canonicalRole(String role) {
        String unprefixedRole = role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role;
        if ("HRManager".equalsIgnoreCase(unprefixedRole)) {
            return "HRManager";
        }
        if ("ADMIN".equalsIgnoreCase(unprefixedRole)) {
            return "ADMIN";
        }
        return unprefixedRole;
    }
}
