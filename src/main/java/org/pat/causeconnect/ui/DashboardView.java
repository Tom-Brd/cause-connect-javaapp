package org.pat.causeconnect.ui;

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
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.entity.task.TaskStatus;
import org.pat.causeconnect.plugin.events.EventManager;
import org.pat.causeconnect.service.AssociationService;
import org.pat.causeconnect.service.project.ProjectService;
import org.pat.causeconnect.service.task.TaskService;
import org.pat.causeconnect.ui.component.CardComponent;
import org.pat.causeconnect.ui.project.ProjectModal;
import org.pat.causeconnect.ui.project.ProjectView;
import org.pat.causeconnect.ui.project.ProjectsView;
import org.pat.causeconnect.ui.task.TaskModal;
import org.pat.causeconnect.ui.task.TaskView;
import org.pat.causeconnect.ui.task.TasksView;
import org.pat.causeconnect.ui.utils.NotificationUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

@Route(value = "", layout = MainLayout.class)
@AnonymousAllowed
@PageTitle("Tableau de bord")
public class DashboardView extends VerticalLayout {

    private final Consumer<String> onCompletion;

    public DashboardView(ProjectService projectService, TaskService taskService, AssociationService associationService, EventManager eventManager) {
        onCompletion = message -> {
            NotificationUtils.createNotification(message, true).open();
            getUI().ifPresent(ui -> ui.getPage().reload());
        };
        setSizeFull();
        ArrayList<Project> projects = projectService.getMyProjects();
        ArrayList<Task> tasks = taskService.getMyTasks();

        createProjectsLayout(projects, projectService);
        createTasksLayout(tasks, associationService, taskService, eventManager);
    }

    private void createTasksLayout(ArrayList<Task> tasks, AssociationService associationService, TaskService taskService, EventManager eventManager) {
        H2 title = new H2("Mes Tâches");
        title.getElement().getStyle().set("margin-top", "32px");
        add(title);

        Button viewAllTasksButton;
        if (tasks.size() > 3) {
            viewAllTasksButton = new Button("Voir plus", e -> getUI().ifPresent(ui -> ui.navigate(TasksView.class)));
            viewAllTasksButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            viewAllTasksButton.getElement().getStyle().set("margin-left", "16px");
        } else {
            viewAllTasksButton = null;
        }

        FlexLayout taskList = new FlexLayout();
        taskList.setWidth("50%");
        taskList.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        taskList.setJustifyContentMode(JustifyContentMode.START);
        taskList.setAlignItems(Alignment.START);

        List<Task> tasksList = tasks
                .stream()
                .filter(task -> task.getStatus() == TaskStatus.TODO)
                .sorted(Comparator.comparing(Task::getDeadline))
                .limit(3)
                .toList();
        tasksList.forEach(task -> {
            Date endTime = task.getDeadline();
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM", Locale.FRENCH);
            CardComponent taskDiv = new CardComponent(task.getProject().getName() + " : " +task.getTitle(), "Échéance : " + formatter.format(endTime));

            ComponentEventListener<AttachEvent> listener = event -> taskDiv.addClickListener(e -> {
                TaskModal taskModal = new TaskModal(task, associationService, taskService, eventManager, onCompletion);
                taskModal.open();
            });
            taskDiv.addAttachListener(listener);
            taskDiv.addClassName("card--task");
            taskList.add(taskDiv);
            if (viewAllTasksButton != null) taskList.add(viewAllTasksButton);
        });

        add(taskList);
    }

    private void createProjectsLayout(ArrayList<Project> projects, ProjectService projectService) {
        add(new H2("Mes Projets"));

        Button createProjectButton = new Button("Créer un projet", e -> {
            ProjectModal projectModal = new ProjectModal(projectService, onCompletion);
            projectModal.open();
        });
        createProjectButton.setIcon(VaadinIcon.PLUS.create());
        createProjectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(createProjectButton);


        Button viewAllProjectsButton;
        if (projects.size() > 3) {
            viewAllProjectsButton = new Button("Voir plus...", e -> getUI().ifPresent(ui -> ui.navigate(ProjectsView.class)));
            viewAllProjectsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            viewAllProjectsButton.getElement().getStyle().set("margin-left", "16px");
        } else {
            viewAllProjectsButton = null;
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
            ComponentEventListener<AttachEvent> listener = event -> projectDiv.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(ProjectView.class, project.getId())));
            projectDiv.addAttachListener(listener);

            projectList.add(projectDiv);
            if (viewAllProjectsButton != null) projectList.add(viewAllProjectsButton);
            projectList.setAlignItems(Alignment.STRETCH);
        });

        add(projectList);
    }
}
