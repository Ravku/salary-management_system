package com.acme.hr.config;

import com.acme.hr.entity.UserAccount;
import com.acme.hr.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class UserAccountBootstrap implements ApplicationRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String hrManagerUsername;
    private final String hrManagerPassword;
    private final String adminUsername;
    private final String adminPassword;

    public UserAccountBootstrap(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.hr-manager.username:}") String hrManagerUsername,
            @Value("${app.bootstrap.hr-manager.password:}") String hrManagerPassword,
            @Value("${app.bootstrap.admin.username:}") String adminUsername,
            @Value("${app.bootstrap.admin.password:}") String adminPassword) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.hrManagerUsername = hrManagerUsername;
        this.hrManagerPassword = hrManagerPassword;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        validateCredentials();
        createAccountIfAbsent(hrManagerUsername, hrManagerPassword, "HRManager");
        createAccountIfAbsent(adminUsername, adminPassword, "ADMIN");
    }

    private void validateCredentials() {
        if (isBlank(hrManagerUsername) || isBlank(hrManagerPassword)
                || isBlank(adminUsername) || isBlank(adminPassword)) {
            throw new IllegalStateException(
                    "Configure bootstrap credentials for both HRManager and ADMIN using app.bootstrap properties.");
        }
        if (hrManagerUsername.trim().equalsIgnoreCase(adminUsername.trim())) {
            throw new IllegalStateException("HRManager and ADMIN bootstrap usernames must be different.");
        }
    }

    private void createAccountIfAbsent(String username, String password, String role) {
        String normalizedUsername = username.trim();
        userAccountRepository.findByUsername(normalizedUsername).ifPresentOrElse(existing -> {
            if (!normalizeRole(role).equals(normalizeRole(existing.getRole()))) {
                throw new IllegalStateException("Existing bootstrap username has a conflicting role: " + role);
            }
        }, () -> {
            UserAccount account = new UserAccount();
            account.setUsername(normalizedUsername);
            account.setPasswordHash(passwordEncoder.encode(password));
            account.setRole(role);
            account.setActive(true);
            userAccountRepository.save(account);
        });
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String normalized = role.trim();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
