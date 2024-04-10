package org.pat.causeconnect.ui.project;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.service.project.ProjectService;
import org.pat.causeconnect.ui.MainLayout;
import org.pat.causeconnect.ui.component.CardComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Route(value = "projects", layout = MainLayout.class)
@AnonymousAllowed
@PageTitle("Mes Projets")
public class ProjectsView extends VerticalLayout {
    public ProjectsView(ProjectService projectService) {

        setSizeFull();
        ArrayList<Project> projects = projectService.getMyProjects();

        add(new H2("Mes Projets"));

        Button createProjectButton = new Button("Créer un projet", e -> {
            ProjectModal projectModal = new ProjectModal(projectService);
            projectModal.open();
        });
        createProjectButton.setIcon(VaadinIcon.PLUS.create());
        createProjectButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        add(createProjectButton);

        FlexLayout projectList = new FlexLayout();
        projectList.setWidthFull();
        projectList.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        List<Project> sortedProjects = projects.stream()
                .sorted(Comparator.comparing(Project::getStartTime))
                .toList();
        sortedProjects.forEach(project -> {
            CardComponent projectDiv = new CardComponent(project.getName(), project.getDescription());
            ComponentEventListener<AttachEvent> listener = event -> projectDiv.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(ProjectView.class, project.getId())));
            projectDiv.addAttachListener(listener);

            projectList.add(projectDiv);
        });

        add(projectList);
    }
}
