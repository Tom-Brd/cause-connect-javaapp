package org.pat.causeconnect.service.auth;

import org.pat.causeconnect.entity.AssociationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AuthenticationService {
    public AuthenticationResponse authenticate(String email, String password) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:3000/auth/login";

        String associationId = AssociationContext.getInstance().getAssociation().getId();

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(email, password, associationId);

        System.out.println("Association ID: " + associationId);

        try {
            return restTemplate.postForObject(url, authenticationRequest, AuthenticationResponse.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                return new AuthenticationResponse(null, "Invalid email or password");
            }
            return new AuthenticationResponse(null, "An error occurred");
        }
    }

    public UserDetailResponse getUserDetails(String token) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:3000/users/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<UserDetailResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserDetailResponse.class);

        return response.getBody();
    }

    public UserDetails createUserDetails(UserDetailResponse userDetail) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER_" + userDetail.getRole().toUpperCase()));

        return new User(
                userDetail.getEmail(),
                "",
                authorities
        );
    }
}
