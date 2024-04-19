package org.pat.causeconnect.plugin;

import org.pat.causeconnect.entity.Plugin;
import org.pat.causeconnect.plugin.events.EventManager;
import org.pat.causeconnect.ui.utils.NotificationUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

@Component
public class PluginLoader {
    private final Map<CauseConnectPlugin, Boolean> pluginsMap = new HashMap<>();
    private final Map<String, CauseConnectPlugin> pluginInstances = new HashMap<>();
    private final Map<String, URLClassLoader> pluginLoaders = new HashMap<>();

    private final EventManager eventManager;

    public PluginLoader(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void loadPlugins() throws Exception {
        System.out.println(System.getProperty("java.class.path"));
        File jarfile = new File(System.getProperty("java.class.path"));

        File pluginsFolder = new File(jarfile.getParentFile(), "plugins");
        File[] plugins = pluginsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (plugins != null) {
            System.out.println("plugins is not null");
            for (File jar : plugins) {
                URLClassLoader loader = URLClassLoader.newInstance(new URL[]{jar.toURI().toURL()}, CauseConnectPlugin.class.getClassLoader());
                ServiceLoader<CauseConnectPlugin> serviceLoader = ServiceLoader.load(CauseConnectPlugin.class, loader);

                for (CauseConnectPlugin plugin : serviceLoader) {
                    String pluginName = jar.getName();
                    pluginInstances.put(pluginName, plugin);
                    pluginLoaders.put(pluginName, loader);
                    System.out.println("plugin: " + plugin);
                    plugin.load(eventManager);
                    pluginsMap.put(plugin, true);

                    for (ViewConfiguration viewConfiguration : plugin.getViews()) {
                        viewConfiguration.registerViews();
                    }
                }
            }
        } else {
            System.out.println("plugins is null");
        }
    }

    public void loadPlugin(String pluginFileName) {
        File jarfile = new File(System.getProperty("java.class.path"));
        File pluginsFolder = new File(jarfile.getParentFile(), "plugins");
        File plugin = new File(pluginsFolder, pluginFileName);

        if (!plugin.exists()) {
            NotificationUtils.createNotification("Le plugin n'existe pas", false).open();
            return;
        }

        try (URLClassLoader loader = URLClassLoader.newInstance(new URL[]{plugin.toURI().toURL()}, CauseConnectPlugin.class.getClassLoader())) {
            ServiceLoader<CauseConnectPlugin> serviceLoader = ServiceLoader.load(CauseConnectPlugin.class, loader);

            CauseConnectPlugin causeConnectPlugin = serviceLoader.iterator().next();

            pluginInstances.put(pluginFileName, causeConnectPlugin);
            pluginLoaders.put(pluginFileName, loader);
            pluginsMap.put(causeConnectPlugin, false);
            causeConnectPlugin.load(eventManager);

            for (ViewConfiguration viewConfiguration : causeConnectPlugin.getViews()) {
                viewConfiguration.registerViews();
            }

            pluginsMap.put(causeConnectPlugin, true);
        } catch (Exception e) {
            NotificationUtils.createNotification("Erreur lors du chargement du plugin", false).open();
        }
    }

    public List<NavItemConfiguration> getAllNavItems() {
        List<NavItemConfiguration> navItems = new ArrayList<>();
        for (CauseConnectPlugin plugin : pluginsMap.keySet()) {
            navItems.addAll(plugin.getNavItems());
        }
        return navItems;
    }

    public void unloadPlugin(String pluginFileName) {
        CauseConnectPlugin causeConnectPlugin = pluginInstances.get(pluginFileName);
        if (causeConnectPlugin != null) {
            try {
                causeConnectPlugin.unload();

                for (ViewConfiguration viewConfiguration : causeConnectPlugin.getViews()) {
                    viewConfiguration.unregisterViews();
                }

                URLClassLoader classLoader = pluginLoaders.get(pluginFileName);

                classLoader.close();

                eventManager.unregisterListener(causeConnectPlugin);

                pluginInstances.remove(pluginFileName);
                pluginLoaders.remove(pluginFileName);
                pluginsMap.remove(causeConnectPlugin);

                System.gc();
            } catch (Exception e) {
                NotificationUtils.createNotification("Erreur lors du déchargement du plugin", false).open();
            }
        } else {
            NotificationUtils.createNotification("Le plugin n'existe pas", false).open();
        }
    }
}
