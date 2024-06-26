package org.pat.causeconnect.service.auth;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.config.InvalidRoleException;
import org.pat.causeconnect.entity.Association;
import org.pat.causeconnect.entity.AssociationContext;
import org.pat.causeconnect.entity.User;
import org.pat.causeconnect.service.AssociationService;
import org.pat.causeconnect.service.user.UserDetailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AuthenticationService {
    @Value("${base.url}")
    private String baseUrl;

    private final AssociationService associationService;

    public AuthenticationService(AssociationService associationService) {
        this.associationService = associationService;
    }

    public Authentication authenticate(String email, String password, String associationId) {
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/auth/login";

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(email, password, associationId);

        try {
            Association association = associationService.getAssociation(associationId);
            AuthenticationResponse response =  restTemplate.postForObject(url, authenticationRequest, AuthenticationResponse.class);

            if (response.getToken() != null && !response.getToken().isEmpty()) {
                VaadinSession.getCurrent().setAttribute("token", response.getToken());
                UserDetailResponse userDetail = getUserDetails(response.getToken());
                User userDetails = createUserDetails(userDetail, association);

                if (userDetails.isExternal()) {
                    throw new InvalidRoleException("External users are not allowed to login");
                }

                return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getRole());
            } else {
                throw new BadCredentialsException(response.getMessage());
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                throw new BadCredentialsException("Invalid credentials");
            }
            throw e;
        }
    }

    public UserDetailResponse getUserDetails(String token) {
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/users/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDetailResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserDetailResponse.class);

        return response.getBody();
    }

    public User createUserDetails(UserDetailResponse userDetail, Association association) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER_" + userDetail.getRole().toUpperCase()));

        return new User(
                userDetail.getId(),
                userDetail.getEmail(),
                userDetail.getFullName(),
                authorities,
                association
        );
    }

    // TODO: Implement password reset
    public void resetPassword(String email) {
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/users/send-password-email";

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest(email, AssociationContext.getInstance().getAssociation().getId());

        restTemplate.postForObject(url, resetPasswordRequest, Void.class);
    }
}
