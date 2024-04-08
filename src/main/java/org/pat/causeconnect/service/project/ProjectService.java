package org.pat.causeconnect.service.project;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.entity.Project;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@Component
public class ProjectService {
    @Value("${base.url}")
    private String baseUrl;

    public ArrayList<Project> getMyProjects() {
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
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/projects";
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Project> entity = new HttpEntity<>(project, headers);

        ResponseEntity<Project> response = restTemplate.exchange(url, HttpMethod.POST, entity, Project.class);

        return response.getBody();
    }
}
