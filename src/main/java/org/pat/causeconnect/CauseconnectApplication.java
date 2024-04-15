package org.pat.causeconnect;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme(value = "causeconnect")
@Push
public class CauseconnectApplication implements AppShellConfigurator {
    public static void main(String[] args) {
        UpdateDownloader updateDownloader = new UpdateDownloader();
        updateDownloader.downloadNewVersion();

        SpringApplication.run(CauseconnectApplication.class, args);
    }
}
