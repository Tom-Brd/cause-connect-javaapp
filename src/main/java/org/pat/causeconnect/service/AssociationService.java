package org.pat.causeconnect.service;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.entity.Association;
import org.pat.causeconnect.entity.User;
import org.pat.causeconnect.service.user.UserDetailResponse;
import org.pat.causeconnect.ui.utils.NotificationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class AssociationService {
    @Value("${base.url}")
    private String baseUrl;

    @Autowired
    private InternetCheckService internetCheckService;

    public Association[] getAssociations() {
        if (!internetCheckService.hasInternetConnection()) {
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/associations";

        return restTemplate.getForObject(url, Association[].class);
    }

    public Association getAssociation(String id) {
        if (!internetCheckService.hasInternetConnection()) {
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/associations/" + id;

        return restTemplate.getForObject(url, Association.class);
    }

    public ArrayList<User> getMembers() {
        if (!internetCheckService.hasInternetConnection()) {
            NotificationUtils.createNotification("Pas de connexion Internet - Aucun membre de l'association récupéré", false);
            return new ArrayList<>();
        }
        
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/users";
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ArrayList<UserDetailResponse>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
        });

        ArrayList<UserDetailResponse> userDetailResponses = response.getBody();

        ArrayList<User> users = new ArrayList<>();
        if (userDetailResponses != null) {
            for (UserDetailResponse userDetailResponse : userDetailResponses) {
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER_" + userDetailResponse.getRole()));
                User user = new User();
                user.setId(userDetailResponse.getId());
                user.setFullName(userDetailResponse.getFullName());
                user.setEmail(userDetailResponse.getEmail());
                user.setRole(authorities);

                users.add(user);
            }
        }
        return users;
    }
}
