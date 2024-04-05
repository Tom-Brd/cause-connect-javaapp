package org.pat.causeconnect.service;

import org.pat.causeconnect.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityService {
    public Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            return Optional.empty();
        }

        return Optional.ofNullable((User) authentication.getPrincipal());
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
