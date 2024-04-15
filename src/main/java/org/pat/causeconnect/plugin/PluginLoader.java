package org.pat.causeconnect.plugin;

import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

@Component
public class PluginLoader {
    private final List<CauseConnectPlugin> pluginsList = new ArrayList<>();
    
    public void loadPlugins() throws Exception {
        System.out.println(System.getProperty("java.class.path"));
        File jarfile = new File(System.getProperty("java.class.path"));

        File pluginsFolder = new File(jarfile.getParentFile(), "plugins");
        File[] plugins = pluginsFolder.listFiles((dir,name) -> name.endsWith(".jar"));
        if(plugins != null) {
            System.out.println("plugins is not null");
            for (File jar : plugins) {
                URLClassLoader loader = URLClassLoader.newInstance(new URL[] {jar.toURI().toURL()}, CauseConnectPlugin.class.getClassLoader());
                ServiceLoader<CauseConnectPlugin> serviceLoader = ServiceLoader.load(CauseConnectPlugin.class, loader);

                for(CauseConnectPlugin plugin : serviceLoader) {
                    pluginsList.add(plugin);
                    
                    plugin.load();

                    for (ViewConfiguration viewConfiguration : plugin.getViews()) {
                        viewConfiguration.registerViews();
                    }
                }
            }
        } else {
            System.out.println("plugins is null");
        }
    }

    public List<NavItemConfiguration> getAllNavItems() {
        List<NavItemConfiguration> navItems = new ArrayList<>();
        for (CauseConnectPlugin plugin : pluginsList) {
            navItems.addAll(plugin.getNavItems());
        }
        return navItems;
    }
}
