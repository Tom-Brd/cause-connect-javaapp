package org.pat.causeconnect.service;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {
    private final AuthenticationContext authenticationContext;

    public SecurityService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public UserDetails getAuthenticatedUser() {
        System.out.println(authenticationContext.getAuthenticatedUser(UserDetails.class));
        return authenticationContext.getAuthenticatedUser(UserDetails.class).isPresent() ?
                authenticationContext.getAuthenticatedUser(UserDetails.class).get() : null;
    }

    public void logout() {
        authenticationContext.logout();
    }
}
