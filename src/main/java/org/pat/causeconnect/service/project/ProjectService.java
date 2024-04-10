package org.pat.causeconnect.service.project;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.service.InternetCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class ProjectService {
    @Value("${base.url}")
    private String baseUrl;

    @Autowired
    private InternetCheckService internetCheckService;

    public ArrayList<Project> getMyProjects() {
        if (!internetCheckService.hasInternetConnection()) {
            return new ArrayList<>();
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/projects/me";
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ParameterizedTypeReference<ArrayList<Project>> responseType = new ParameterizedTypeReference<ArrayList<Project>>() {
        };
        ResponseEntity<ArrayList<Project>> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);

        return response.getBody();
    }

    public Project getProjectById(String projectId) {
        if (!internetCheckService.hasInternetConnection()) {
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/projects/" + projectId;
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Project> response = restTemplate.exchange(url, HttpMethod.GET, entity, Project.class);

        return response.getBody();
    }

    public Project createProject(Project project) {
        if (!internetCheckService.hasInternetConnection()) {
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/projects";
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Project> entity = new HttpEntity<>(project, headers);

        ResponseEntity<Project> response = restTemplate.exchange(url, HttpMethod.POST, entity, Project.class);

        return response.getBody();
    }

    public Project updateProject(Project project) {
        if (!internetCheckService.hasInternetConnection()) {
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(requestFactory);

        String url = baseUrl + "/projects/" + project.getId();
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Project> entity = new HttpEntity<>(project, headers);

        ResponseEntity<Project> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, Project.class);

        return response.getBody();
    }
}
