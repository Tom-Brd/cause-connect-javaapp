package org.pat.causeconnect.service;

import org.pat.causeconnect.entity.Association;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AssociationService {
    @Value("${base.url}")
    private String baseUrl;

    public Association[] getAssociations() {
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/associations";

        return restTemplate.getForObject(url, Association[].class);
    }

    public Association getAssociation(String id) {
        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/associations/" + id;

        return restTemplate.getForObject(url, Association.class);
    }
}
