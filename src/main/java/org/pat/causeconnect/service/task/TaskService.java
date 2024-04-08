package org.pat.causeconnect.service.task;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.entity.User;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.entity.task.TaskStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Component
public class TaskService {
    @Value("${base.url}")
    private String baseUrl;

    public ArrayList<Task> getMyTasks() {
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
        return tasks;
    }

    public Task getTaskById(String taskId) {
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

        return getTask(taskResponse, status);
    }

    private static Task getTask(TaskResponse taskResponse, TaskStatus status) {
        User responsibleUser = new User();
        responsibleUser.setId(taskResponse.getResponsibleUser().getId());
        responsibleUser.setEmail(taskResponse.getResponsibleUser().getEmail());
        responsibleUser.setFullName(taskResponse.getResponsibleUser().getFullName());

        return new Task(
                taskResponse.getId(),
                taskResponse.getTitle(),
                taskResponse.getDescription(),
                status,
                taskResponse.getDeadline(),
                responsibleUser,
                taskResponse.getProject()
        );
    }

    public Task updateTask(Task task) {
        System.out.println(task.getId());
        System.out.println(task.getProject().getId());
        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(requestFactory);

        String url = baseUrl + "/tasks/" + task.getId();
        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // convert Date to this format 2024-04-08T13:40:34.457Z
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        String formattedDate = formatter.format(task.getDeadline().toInstant().atOffset(ZoneOffset.UTC));
        System.out.println(formattedDate);
        TaskUpdateRequest taskRequest = new TaskUpdateRequest(
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name().toLowerCase(),
                formattedDate,
                task.getProject().getId(),
                task.getResponsibleUser().getId()
        );

        HttpEntity<TaskUpdateRequest> entity = new HttpEntity<>(taskRequest, headers);

        ResponseEntity<TaskResponse> response = restTemplate.exchange(url, HttpMethod.PATCH, entity, TaskResponse.class);

        TaskResponse taskResponse = response.getBody();
        if (taskResponse == null) {
            return null;
        }
        TaskStatus status = TaskStatus.valueOf(taskResponse.getStatus().toUpperCase());

        return getTask(taskResponse, status);
    }
}