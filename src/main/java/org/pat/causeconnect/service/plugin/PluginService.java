package org.pat.causeconnect.service.plugin;

import com.vaadin.flow.server.VaadinSession;
import org.pat.causeconnect.entity.Plugin;
import org.pat.causeconnect.plugin.PluginLoader;
import org.pat.causeconnect.service.InternetCheckService;
import org.pat.causeconnect.service.project.MyProjectsResponse;
import org.pat.causeconnect.service.project.ProjectByIdResponse;
import org.pat.causeconnect.ui.utils.NotificationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

@Service
public class PluginService {
    @Value("${base.url}")
    private String baseUrl;

    private final InternetCheckService internetCheckService;
    private final PluginLoader pluginLoader;

    public PluginService(InternetCheckService internetCheckService, PluginLoader pluginLoader) {
        this.internetCheckService = internetCheckService;
        this.pluginLoader = pluginLoader;
    }

    public ArrayList<Plugin> getPlugins() {
        if (!internetCheckService.hasInternetConnection()) {
            PluginsResponse plugins = VaadinSession.getCurrent().getAttribute(PluginsResponse.class);

            if (plugins == null) {
                NotificationUtils.createNotification("Pas de connexion Internet, aucune information n'a pu être chargée", false).open();
                return null;
            }

            return plugins.getPlugins();
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = baseUrl + "/java-app/plugins";

        ParameterizedTypeReference<ArrayList<Plugin>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<ArrayList<Plugin>> plugins = restTemplate.exchange(url, HttpMethod.GET, null, responseType);

        PluginsResponse pluginsResponse = new PluginsResponse(plugins.getBody());

        VaadinSession.getCurrent().setAttribute(PluginsResponse.class, pluginsResponse);

        return pluginsResponse.getPlugins();
    }

    public boolean isPluginInstalled(String jarFilePath) {
        Path pluginPath = getPluginPath();
        String fileName = getFileName(jarFilePath);

        return Files.exists(pluginPath.resolve(fileName));
    }

    private Path getPluginPath() {
        return Paths.get("/Applications/CauseConnect.app/Contents/app/plugins/");
    }

    private String getFileName(String jarFilePath) {
        String[] parts = jarFilePath.split("/");
        return parts[parts.length - 1];
    }

    public void installPlugin(Plugin plugin) {
        if (!internetCheckService.hasInternetConnection()) {
            NotificationUtils.createNotification("Pas de connexion Internet, l'installation a échoué", false).open();
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        String downloadUrl = baseUrl + "/java-app/plugins/" + plugin.getId();

        try {
            Resource resource = restTemplate.getForObject(downloadUrl, Resource.class);
            if (resource == null || !resource.isReadable()) {
                NotificationUtils.createNotification("Erreur lors de la récupération du plugin", false).open();
                return;
            }

            Path tempPath = Files.createTempFile(plugin.getName(), ".jar");
            resource.getInputStream().transferTo(Files.newOutputStream(tempPath));

            Path pluginPath = getPluginPath();
            String fileName = getFileName(plugin.getJarFilePath());
            Files.move(tempPath, pluginPath.resolve(fileName));

            pluginLoader.loadPlugin(fileName);

            NotificationUtils.createNotification("Plugin installé avec succès", true).open();
        } catch (Exception e) {
            NotificationUtils.createNotification("Erreur lors de l'installation du plugin" + e.getMessage(), false).open();
        }
    }

    public void deletePlugin(Plugin plugin) {
        Path pluginPath = getPluginPath();
        String fileName = getFileName(plugin.getJarFilePath());
        System.out.println("Deleting plugin: " + fileName);
        try {
            pluginLoader.unloadPlugin(fileName);
            Files.deleteIfExists(pluginPath.resolve(fileName));
            NotificationUtils.createNotification("Plugin supprimé", true).open();
        } catch (Exception e) {
            NotificationUtils.createNotification("Erreur lors de la suppression du plugin", false).open();
        }
    }
}
