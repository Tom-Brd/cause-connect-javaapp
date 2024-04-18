package org.pat.causeconnect.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.pat.causeconnect.plugin.PluginLoader;
import org.springframework.stereotype.Component;

@Component
public class ApplicationServiceInitListener implements VaadinServiceInitListener {
    private final PluginLoader pluginLoader;

    public ApplicationServiceInitListener(PluginLoader pluginLoader) {
        this.pluginLoader = pluginLoader;
    }

    @Override
    public void serviceInit(ServiceInitEvent serviceInitEvent) {
        System.out.println("In serviceInit");
        try {
            pluginLoader.loadPlugins();
        } catch (Exception e) {
            System.out.println("Error loading plugins : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
