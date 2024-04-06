package org.pat.causeconnect.ui.project;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.service.project.ProjectService;
import org.pat.causeconnect.ui.MainLayout;

@Route(value = "project", layout = MainLayout.class)
@AnonymousAllowed
@PageTitle("Project")
public class ProjectView extends VerticalLayout implements HasUrlParameter<String> {

    private final ProjectService projectService;

    public ProjectView(ProjectService projectService) {
        this.projectService = projectService;
        add("Project view");
    }

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        loadProject(parameter);
    }

    private void loadProject(String projectId) {
        Project project = projectService.getProjectById(projectId);
        add(project.getName());
    }
}
