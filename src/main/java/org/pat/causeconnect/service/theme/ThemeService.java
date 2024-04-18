package org.pat.causeconnect.service.theme;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.entity.Theme;
import org.pat.causeconnect.service.InternetCheckService;
import org.pat.causeconnect.service.task.TaskUpdateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ThemeService {
    @Value("${base.url}")
    private String baseUrl;

    private final InternetCheckService internetCheckService;

    public ThemeService(InternetCheckService internetCheckService) {
        this.internetCheckService = internetCheckService;
    }

    public Theme getTheme() {
        if (!internetCheckService.hasInternetConnection()) {
            return VaadinSession.getCurrent().getAttribute(Theme.class);
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/settings/theme";

        String token = VaadinSession.getCurrent().getAttribute("token").toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<TaskUpdateRequest> entity = new HttpEntity<>(headers);

        ResponseEntity<Theme> response = restTemplate.exchange(url, HttpMethod.GET, entity, Theme.class);

        VaadinSession.getCurrent().setAttribute(Theme.class, response.getBody());

        return response.getBody();
    }
}
