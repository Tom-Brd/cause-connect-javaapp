package org.pat.causeconnect;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import org.pat.causeconnect.entity.CauseConnectPlugin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

@SpringBootApplication
@Theme(value = "causeconnect")
@Push
public class CauseconnectApplication implements AppShellConfigurator {
    public static void main(String[] args) {
        UpdateDownloader updateDownloader = new UpdateDownloader();
        updateDownloader.downloadNewVersion();

        SpringApplication.run(CauseconnectApplication.class, args);
        try {
            loadPlugins();
        } catch (Exception e) {
            System.out.println("Error loading plugins");
        }
    }

    public static void loadPlugins() throws Exception {
        System.out.println(System.getProperty("java.class.path"));
        File jarfile = new File(System.getProperty("java.class.path"));

        File pluginsFolder = new File(jarfile.getParentFile(), "plugins");
        File[] plugins = pluginsFolder.listFiles((dir,name) -> name.endsWith(".jar"));
        if(plugins != null) {
            System.out.println("plugins is not null");
            for (File jar : plugins) {
                System.out.println("jar: " + jar);
                URLClassLoader loader = URLClassLoader.newInstance(new URL[] {jar.toURI().toURL()}, CauseConnectPlugin.class.getClassLoader());
                ServiceLoader<CauseConnectPlugin> serviceLoader = ServiceLoader.load(CauseConnectPlugin.class, loader);

                for(CauseConnectPlugin plugin : serviceLoader) {
                    System.out.println("plugin: " + plugin);
                    plugin.load();
                }
            }
        } else {
            System.out.println("plugins is null");
        }
    }
}
