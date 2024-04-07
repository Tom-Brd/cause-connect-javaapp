package org.pat.causeconnect.service.task;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.entity.User;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.entity.task.TaskStatus;
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
}