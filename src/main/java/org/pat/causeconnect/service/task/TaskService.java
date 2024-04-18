package org.pat.causeconnect.service.task;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.entity.User;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.entity.task.TaskStatus;
import org.pat.causeconnect.plugin.events.EventManager;
import org.pat.causeconnect.plugin.events.task.GettingMyTasksEvent;
import org.pat.causeconnect.plugin.events.task.GettingSingleTaskEvent;
import org.pat.causeconnect.plugin.events.task.GettingTasksEvent;
import org.pat.causeconnect.service.InternetCheckService;
import org.pat.causeconnect.ui.utils.NotificationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class TaskService {
    @Value("${base.url}")
    private String baseUrl;

    private final InternetCheckService internetCheckService;
    private final EventManager eventManager;

    public TaskService(InternetCheckService internetCheckService, EventManager eventManager) {
        this.internetCheckService = internetCheckService;
        this.eventManager = eventManager;
    }

    public ArrayList<Task> getMyTasks() {
        if (!internetCheckService.hasInternetConnection()) {
            MyTasksResponse myTasksResponse = VaadinSession.getCurrent().getAttribute(MyTasksResponse.class);

            if (myTasksResponse == null) {
                NotificationUtils.createNotification("Pas de connexion Internet, aucune information n'a pu être chargée", false).open();
                return null;
            }

            return myTasksResponse.getTaskResponses();
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/tasks/me";
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ArrayList<TaskResponse>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
        });

        ArrayList<TaskResponse> taskResponses = response.getBody();

        ArrayList<Task> tasks = new ArrayList<>();
        if (taskResponses != null) {
            for (TaskResponse taskResponse : taskResponses) {
                TaskStatus status = TaskStatus.valueOf(taskResponse.getStatus().toUpperCase());

                Task task = getTask(taskResponse, status);

                tasks.add(task);
            }
        }

        MyTasksResponse myTasksResponse = new MyTasksResponse(tasks);
        VaadinSession.getCurrent().setAttribute(MyTasksResponse.class, myTasksResponse);

        GettingMyTasksEvent event = new GettingMyTasksEvent(tasks);
        eventManager.fireEvent(event);

        return tasks;
    }

    public Task getTaskById(String taskId) {
        if (!internetCheckService.hasInternetConnection()) {
            Map<String, Task> tasksById = (Map<String, Task>)
                    VaadinSession.getCurrent().getAttribute("tasksById");

            if (tasksById == null || !tasksById.containsKey(taskId)) {
                NotificationUtils.createNotification("Pas de connexion Internet, aucune information n'a pu être chargée", false).open();
                return null;
            }

            return tasksById.get(taskId);
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/tasks/" + taskId;
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<TaskResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, TaskResponse.class);

        TaskResponse taskResponse = response.getBody();
        if (taskResponse == null) {
            return null;
        }
        TaskStatus status = TaskStatus.valueOf(taskResponse.getStatus().toUpperCase());

        Task task = getTask(taskResponse, status);
        Map<String, Task> tasksById = (Map<String, Task>) VaadinSession.getCurrent().getAttribute("tasksById");
        if (tasksById == null) {
            tasksById = new HashMap<>();
        }

        tasksById.put(taskId, task);
        VaadinSession.getCurrent().setAttribute("tasksById", tasksById);

        GettingSingleTaskEvent event = new GettingSingleTaskEvent(task);
        eventManager.fireEvent(event);

        return task;
    }

    private static Task getTask(TaskResponse taskResponse, TaskStatus status) {
        User responsibleUser;
        if (taskResponse.getResponsibleUser() != null) {
            responsibleUser = new User();
            responsibleUser.setId(taskResponse.getResponsibleUser().getId());
            responsibleUser.setEmail(taskResponse.getResponsibleUser().getEmail());
            responsibleUser.setFullName(taskResponse.getResponsibleUser().getFullName());
        } else {
            responsibleUser = null;
        }

        Project project;
        if (taskResponse.getProject() != null) {
            project = new Project();
            project.setId(taskResponse.getProject().getId());
            project.setName(taskResponse.getProject().getName());
            project.setDescription(taskResponse.getProject().getDescription());
            project.setStartTime(taskResponse.getProject().getStartTime());
            project.setEndTime(taskResponse.getProject().getEndTime());
        } else {
            project = null;
        }

        return new Task(
                taskResponse.getId(),
                taskResponse.getTitle(),
                taskResponse.getDescription(),
                status,
                taskResponse.getDeadline(),
                responsibleUser,
                project
        );
    }

    public Task updateTask(Task task) {
        if (!internetCheckService.hasInternetConnection()) {
            NotificationUtils.createNotification("Pas de connexion Internet - Les modifications n'ont pas été effectuées", false).open();
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(requestFactory);

        String url = baseUrl + "/tasks/" + task.getId();
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        String formattedDate = formatter.format(task.getDeadline().toInstant().atOffset(ZoneOffset.UTC));

        TaskUpdateRequest taskRequest = new TaskUpdateRequest();
        taskRequest.setTitle(task.getTitle());
        taskRequest.setDescription(task.getDescription());
        taskRequest.setStatus(task.getStatus().name().toLowerCase());
        taskRequest.setDeadline(formattedDate);
        taskRequest.setProjectId(task.getProject().getId());
        taskRequest.setResponsibleUserId(task.getResponsibleUser() != null ? task.getResponsibleUser().getId() : "");

        HttpEntity<TaskUpdateRequest> entity = new HttpEntity<>(taskRequest, headers);

        ResponseEntity<TaskResponse> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, TaskResponse.class);

        TaskResponse taskResponse = response.getBody();
        if (taskResponse == null) {
            NotificationUtils.createNotification("Erreur lors de la sauvegarde des modifications", false).open();
            return null;
        }
        TaskStatus status = TaskStatus.valueOf(taskResponse.getStatus().toUpperCase());

        return getTask(taskResponse, status);
    }

    public Task createTask(Task task) {
        if (!internetCheckService.hasInternetConnection()) {
            NotificationUtils.createNotification("Pas de connexion Internet - La tâche n'a pas pu être créée", false).open();
            return null;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(requestFactory);

        String url = baseUrl + "/projects/" + task.getProject().getId() + "/tasks";
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // convert Date to this format 2024-04-08T13:40:34.457Z
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        String formattedDate = formatter.format(task.getDeadline().toInstant().atOffset(ZoneOffset.UTC));
        TaskCreateRequest taskRequest = new TaskCreateRequest(
                task.getTitle(),
                task.getDescription(),
                formattedDate
        );

        HttpEntity<TaskCreateRequest> entity = new HttpEntity<>(taskRequest, headers);

        ResponseEntity<TaskResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, TaskResponse.class);

        TaskResponse taskResponse = response.getBody();
        if (taskResponse == null) {
            NotificationUtils.createNotification("Erreur lors de la création de la tâche", false).open();
            return null;
        }
        TaskStatus status = TaskStatus.valueOf(taskResponse.getStatus().toUpperCase());

        return getTask(taskResponse, status);
    }

    public void assignTask(Task task, User user) {
        if (!internetCheckService.hasInternetConnection()) {
            NotificationUtils.createNotification("Pas de connexion internet - la tâche n'a pas pu être assignée", false).open();
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(requestFactory);

        String url = baseUrl + "/tasks/" + task.getId() + "/responsible-user";
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"userId\": \"" + user.getId() + "\"}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
    }

    public ArrayList<Task> getTasksByProject(Project project) {
        if (!internetCheckService.hasInternetConnection()) {
            Map<Project, ArrayList<Task>> tasksByProject = (Map<Project, ArrayList<Task>>)
                    VaadinSession.getCurrent().getAttribute("tasksByProject");

            if (tasksByProject == null || !tasksByProject.containsKey(project)) {
                NotificationUtils.createNotification("Pas de connexion Internet, aucune information n'a pu être chargée", false).open();
                return null;
            }

            return tasksByProject.get(project);
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/projects/" + project.getId() + "/tasks";
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<ArrayList<TaskResponse>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
        });

        ArrayList<TaskResponse> taskResponses = response.getBody();

        ArrayList<Task> tasks = new ArrayList<>();
        if (taskResponses != null) {
            for (TaskResponse taskResponse : taskResponses) {
                TaskStatus status = TaskStatus.valueOf(taskResponse.getStatus().toUpperCase());

                Task task = getTask(taskResponse, status);
                task.setProject(project);

                tasks.add(task);
            }
        }

        Map<Project, ArrayList<Task>> tasksByProject = (Map<Project, ArrayList<Task>>) VaadinSession.getCurrent().getAttribute("tasksByProject");
        if (tasksByProject == null) {
            tasksByProject = new HashMap<>();
        }

        tasksByProject.put(project, tasks);
        VaadinSession.getCurrent().setAttribute("tasksByProject", tasksByProject);

        GettingTasksEvent event = new GettingTasksEvent(tasks);
        eventManager.fireEvent(event);

        return tasks;
    }

    public boolean deleteTask(Task task) {
        if (!internetCheckService.hasInternetConnection()) {
            NotificationUtils.createNotification("Pas de connexion Internet - La tâche n'a pas pu être supprimée", false).open();
            return false;
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/tasks/" + task.getId();
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<TaskResponse> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, TaskResponse.class);

        return response.getStatusCode().equals(HttpStatus.OK);
    }
}