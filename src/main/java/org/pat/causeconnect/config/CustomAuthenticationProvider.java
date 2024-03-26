package org.pat.causeconnect.config;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.service.auth.AuthenticationResponse;
import org.pat.causeconnect.service.auth.AuthenticationService;
import org.pat.causeconnect.service.auth.UserDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        AuthenticationResponse response = authenticationService.authenticate(email, password);

        if (response.getToken() != null && !response.getToken().isEmpty()) {
            UserDetailResponse userDetail = authenticationService.getUserDetails(response.getToken());
            UserDetails userDetails = authenticationService.createUserDetails(userDetail);

            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } else {
            throw new BadCredentialsException(response.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
