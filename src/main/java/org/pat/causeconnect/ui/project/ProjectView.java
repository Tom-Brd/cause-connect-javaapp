package org.pat.causeconnect.ui.project;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.entity.task.TaskStatus;
import org.pat.causeconnect.service.AssociationService;
import org.pat.causeconnect.service.project.ProjectService;
import org.pat.causeconnect.service.task.TaskService;
import org.pat.causeconnect.ui.MainLayout;
import org.pat.causeconnect.ui.component.CardComponent;
import org.pat.causeconnect.ui.task.TaskModal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Route(value = "project", layout = MainLayout.class)
@AnonymousAllowed
@PageTitle("Project")
public class ProjectView extends VerticalLayout implements HasUrlParameter<String> {

    private final Tabs tabs = new Tabs();
    private final Div kanbanView = new Div();
    private final Div configView = new Div();
    private final Div todoColumn = new Div();
    private final Div inProgressColumn = new Div();
    private final Div doneColumn = new Div();

    private final TaskService taskService;
    private final AssociationService associationService;

    private String projectId;

    public ProjectView(ProjectService projectService, TaskService taskService, AssociationService associationService) {
        this.taskService = taskService;
        this.associationService = associationService;

        kanbanView.setWidthFull();

        setUpTabs();
    }

    private void setUpTabs() {
        Tab kanbanTab = new Tab("Kanban");
        Tab configTab = new Tab("Configuration");

        Map<Tab, Component> tabToPageMap = new HashMap<>();
        tabToPageMap.put(kanbanTab, kanbanView);
        tabToPageMap.put(configTab, configView);

        tabs.add(kanbanTab, configTab);
        tabs.addSelectedChangeListener(event -> {
            tabToPageMap.values().forEach(page -> page.setVisible(false));
            tabToPageMap.get(tabs.getSelectedTab()).setVisible(true);
        });

        kanbanView.setVisible(true);
        add(tabs, kanbanView, configView);
    }

    @Override
    public void setParameter(BeforeEvent event, String projectId) {
        this.projectId = projectId;
        buildKanbanView();
    }

    private void buildKanbanView() {
        kanbanView.removeAll();

        setUpKanbanColumns();

        HorizontalLayout columns = new HorizontalLayout(todoColumn, inProgressColumn, doneColumn);
        columns.setFlexGrow(1, todoColumn, inProgressColumn, doneColumn);
        columns.setSizeFull();

        kanbanView.add(columns);
        loadTasks();
    }

    private void setUpKanbanColumns() {
        createStatusColumn(todoColumn, "TO DO", TaskStatus.TODO);
        createStatusColumn(inProgressColumn, "IN PROGRESS", TaskStatus.IN_PROGRESS);
        createStatusColumn(doneColumn, "DONE", TaskStatus.DONE);
    }

    private void createStatusColumn(Div column, String title, TaskStatus status) {
        column.removeAll();
        column.addClassName("kanban-column");
        column.setWidth("33%");
        column.getElement().setAttribute("status", status.toString());
        column.add(new H3(title));
    }

    private void loadTasks() {
        ArrayList<Task> tasks = taskService.getTasksByProjectId(projectId);
        tasks.forEach(this::addTaskToColumn);
    }

    private void addTaskToColumn(Task task) {
        CardComponent card = new CardComponent(task.getTitle(), task.getDescription());
        ComponentEventListener<AttachEvent> listener = event -> card.addClickListener(e -> {
            TaskModal taskModal = new TaskModal(task, associationService, taskService);
            taskModal.open();
        });
        card.addAttachListener(listener);

        Div column = getColumnForTaskStatus(task.getStatus());
        column.add(card);
    }

    private Div getColumnForTaskStatus(TaskStatus status) {
        return switch (status) {
            case TODO -> todoColumn;
            case IN_PROGRESS -> inProgressColumn;
            case DONE -> doneColumn;
        };
    }
}
