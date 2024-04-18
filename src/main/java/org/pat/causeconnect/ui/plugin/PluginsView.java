package org.pat.causeconnect.ui.plugin;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.pat.causeconnect.entity.Plugin;
import org.pat.causeconnect.service.plugin.PluginService;
import org.pat.causeconnect.ui.MainLayout;
import org.pat.causeconnect.ui.utils.NotificationUtils;

import java.util.List;

@Route(value = "plugins", layout = MainLayout.class)
@AnonymousAllowed
@PageTitle("Plugins")
public class PluginsView extends VerticalLayout {
    private Grid<Plugin> grid;
    private TextField nameFilter;
    private TextField authorFilter;
    private final PluginService pluginService;
    private final List<Plugin> plugins;

    public PluginsView(PluginService pluginService) {
        this.pluginService = pluginService;
        plugins = pluginService.getPlugins();
        setSizeFull();
        addClassName("plugins-view");

        configureFilters();
        add(createTopBar(), createGrid());
        updateList();
    }

    private HorizontalLayout createTopBar() {
        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidthFull();
        topLayout.setPadding(true);
        topLayout.setSpacing(true);
        topLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END);
        topLayout.add(nameFilter, authorFilter);
        return topLayout;
    }

    private void configureFilters() {
        nameFilter = new TextField("Nom");
        nameFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
        nameFilter.setClearButtonVisible(true);
        nameFilter.setValueChangeMode(ValueChangeMode.LAZY);
        nameFilter.setWidth("25%");
        nameFilter.addKeyPressListener(keyPressEvent -> {
            if (keyPressEvent.getKey().equals(Key.ENTER)) {
                updateList();
            }
        });

        authorFilter = new TextField("Développeur");
        authorFilter.setPrefixComponent(VaadinIcon.SEARCH.create());
        authorFilter.setClearButtonVisible(true);
        authorFilter.setValueChangeMode(ValueChangeMode.LAZY);
        authorFilter.setWidth("25%");
        authorFilter.addKeyPressListener(keyPressEvent -> {
            if (keyPressEvent.getKey().equals(Key.ENTER)) {
                updateList();
            }
        });
    }

    private Grid<Plugin> createGrid() {
        grid = new Grid<>(Plugin.class, false);
        grid.addClassNames("plugin-grid");
        grid.setSizeFull();
        grid.addColumn(Plugin::getName).setHeader("Nom").setAutoWidth(true);
        grid.addColumn(Plugin::getAuthor).setHeader("Développeur").setAutoWidth(true);
        grid.addColumn(Plugin::getDescription).setHeader("Description").setAutoWidth(true);
        grid.addComponentColumn(this::createCallApiButton).setHeader("Actions").setAutoWidth(true);
        return grid;
    }

    private Button createCallApiButton(Plugin plugin) {
        boolean isPluginInstalled = pluginService.isPluginInstalled(plugin.getJarFilePath());
        if (isPluginInstalled) {
            Button deleteButton = new Button("Supprimer", VaadinIcon.TRASH.create(), click -> {
                pluginService.deletePlugin(plugin);
                getUI().ifPresent(ui -> ui.access(() -> {
                    updateList();
                    ui.push();
                }));
                NotificationUtils.createNotification("Plugin supprimé", true).open();
            });
            deleteButton.getElement().getStyle().set("color", "white");
            deleteButton.getElement().getStyle().set("background-color", "#FF4D4F");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON);
            return deleteButton;
        }
        return new Button("Télécharger", click -> {
            pluginService.installPlugin(plugin);
            getUI().ifPresent(ui -> ui.access(() -> {
                updateList();
                ui.push();
            }));
            NotificationUtils.createNotification("Calling API for " + plugin.getName(), true).open();
        });
    }

    private void updateList() {
        List<Plugin> filteredPlugins = plugins.stream()
                .filter(plugin -> nameFilter.getValue().isEmpty() || plugin.getName().toLowerCase().contains(nameFilter.getValue().toLowerCase()))
                .filter(plugin -> authorFilter.getValue().isEmpty() || plugin.getAuthor().toLowerCase().contains(authorFilter.getValue().toLowerCase()))
                .toList();
        grid.setItems(filteredPlugins);
    }
}
