package org.pat.causeconnect.service.project;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.service.InternetCheckService;
import org.pat.causeconnect.ui.utils.NotificationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProjectService {
    @Value("${base.url}")
    private String baseUrl;

    @Autowired
    private InternetCheckService internetCheckService;

    public ArrayList<Project> getMyProjects() {
        if (!internetCheckService.hasInternetConnection()) {
            MyProjectsResponse myProjectsResponse = VaadinSession.getCurrent().getAttribute(MyProjectsResponse.class);

            if (myProjectsResponse == null) {
                NotificationUtils.createNotification("Pas de connexion Internet, aucune information n'a pu être chargée", false).open();
                return null;
            }

            return myProjectsResponse.getMyProjects();
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/projects/me";
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ParameterizedTypeReference<ArrayList<Project>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<ArrayList<Project>> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);

        MyProjectsResponse myProjectsResponse = new MyProjectsResponse(response.getBody());

        VaadinSession.getCurrent().setAttribute(MyProjectsResponse.class, myProjectsResponse);

        return myProjectsResponse.getMyProjects();
    }

    public Project getProjectById(String projectId) {
        if (!internetCheckService.hasInternetConnection()) {
            Map<String, ProjectByIdResponse> projectsById = (Map<String, ProjectByIdResponse>)
                    VaadinSession.getCurrent().getAttribute("projectsById");

            if (projectsById == null || !projectsById.containsKey(projectId)) {
                NotificationUtils.createNotification("Pas de connexion Internet, aucune information n'a pu être chargée", false).open();
                return null;
            }

            return projectsById.get(projectId).getProject();
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/projects/" + projectId;
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Project> response = restTemplate.exchange(url, HttpMethod.GET, entity, Project.class);

        ProjectByIdResponse projectByIdResponse = new ProjectByIdResponse(response.getBody());

        Map<String, ProjectByIdResponse> projectsById = (Map<String, ProjectByIdResponse>) VaadinSession.getCurrent().getAttribute("projectsById");
        if (projectsById == null) {
            projectsById = new HashMap<>();
        }

        projectsById.put(projectId, projectByIdResponse);
        VaadinSession.getCurrent().setAttribute("projectsById", projectsById);

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

    public boolean deleteProject(String projectId) {
        if (!internetCheckService.hasInternetConnection()) {
            NotificationUtils.createNotification("Pas de connexion Internet, impossible de supprimer le projet", false).open();
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/projects/" + projectId;
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Project> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Project.class);

        return response.getStatusCode().equals(HttpStatus.OK);
    }
}
