package org.pat.causeconnect.service;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityService {
    private final AuthenticationContext authenticationContext;

    public SecurityService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public Optional<UserDetails> getAuthenticatedUser() {
        System.out.println(authenticationContext.getAuthenticatedUser(UserDetails.class));
        return authenticationContext.getAuthenticatedUser(UserDetails.class);
    }

    public void logout() {
        authenticationContext.logout();
    }
}
