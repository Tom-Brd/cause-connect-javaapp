package org.pat.causeconnect.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.service.project.ProjectService;
import org.pat.causeconnect.ui.component.CardComponent;
import org.pat.causeconnect.ui.project.ProjectView;
import org.pat.causeconnect.ui.project.ProjectsView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@AnonymousAllowed
@PageTitle("Dashboard")
public class DashboardView extends VerticalLayout {
    public DashboardView(ProjectService projectService) {
        setSizeFull();
        ArrayList<Project> projects = projectService.getMyProjects();

        add(new H2("My Projects"));

        Button viewAllButton;
        if (projects.size() > 3) {
            viewAllButton = new Button("Voir plus...", e -> {
                getUI().ifPresent(ui -> ui.navigate(ProjectsView.class));
            });
            viewAllButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        } else {
            viewAllButton = null;
        }

        FlexLayout projectList = new FlexLayout();
        projectList.setWidthFull();
        projectList.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        List<Project> sortedProjects = projects.stream()
                .sorted(Comparator.comparing(Project::getStartTime))
                .toList();
        List<Project> limitedProjects = sortedProjects.stream()
                        .limit(3)
                        .toList();
        limitedProjects.forEach(project -> {
            CardComponent projectDiv = new CardComponent(project.getName(), project.getDescription());
            ComponentEventListener<AttachEvent> listener = event -> {
                projectDiv.addClickListener(e -> {
                    getUI().ifPresent(ui -> ui.navigate(ProjectView.class, project.getId()));
                });
            };
            projectDiv.addAttachListener(listener);

            projectList.add(projectDiv);
            projectList.add(viewAllButton);
            projectList.setAlignItems(Alignment.CENTER);
        });

        add(projectList);
    }
}
