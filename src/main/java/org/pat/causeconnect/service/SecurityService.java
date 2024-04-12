package org.pat.causeconnect.service;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityService {
    @Autowired
    private ApplicationContext applicationContext;

    public Optional<User> getAuthenticatedUser() {
        Authentication authentication = VaadinSession.getCurrent().getAttribute(Authentication.class);

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            return Optional.empty();
        }

        return Optional.ofNullable((User) authentication.getPrincipal());
    }

    public void logout() {
        VaadinSession.getCurrent().getSession().invalidate();
    }

    public void shutdown() {
        SpringApplication.exit(applicationContext, () -> 0);
    }
}
