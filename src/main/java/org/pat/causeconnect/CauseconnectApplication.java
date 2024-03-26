package org.pat.causeconnect;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.pat.causeconnect.entity.Association;
import org.pat.causeconnect.entity.AssociationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

@SpringBootApplication
public class CauseconnectApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        Properties prop = new Properties();

        String propertiesFileName = "association.properties";
        String externalPropertiesPath = Paths.get(System.getProperty("user.dir"), propertiesFileName).toString();
        try {
            FileInputStream ip = new FileInputStream(externalPropertiesPath);
            prop.load(ip);

            String associationId = prop.getProperty("association_id");

            if (associationId == null || associationId.isEmpty()) {
                throw new RuntimeException("Association ID not found in properties file");
            }

            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:3000/associations/" + associationId;
            Association association = restTemplate.getForObject(url, Association.class);

            AssociationContext.getInstance().setAssociation(association);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SpringApplication.run(CauseconnectApplication.class, args);
    }

}
