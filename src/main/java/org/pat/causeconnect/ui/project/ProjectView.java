package org.pat.causeconnect.ui.project;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.entity.task.TaskStatus;
import org.pat.causeconnect.service.AssociationService;
import org.pat.causeconnect.service.project.ProjectService;
import org.pat.causeconnect.service.task.TaskService;
import org.pat.causeconnect.ui.MainLayout;
import org.pat.causeconnect.ui.component.CardComponentDraggable;
import org.pat.causeconnect.ui.task.TaskModal;
import org.pat.causeconnect.ui.utils.NotificationUtils;

import java.time.ZoneId;
import java.util.*;

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

    private final ProjectService projectService;
    private final TaskService taskService;
    private final AssociationService associationService;

    private String projectId;
    private Project project;

    public ProjectView(ProjectService projectService, TaskService taskService, AssociationService associationService) {
        this.projectService = projectService;
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
        configView.setVisible(false);
        add(tabs, kanbanView, configView);
    }

    @Override
    public void setParameter(BeforeEvent event, String projectId) {
        this.projectId = projectId;
        this.project = projectService.getProjectById(projectId);

        buildKanbanView();
        buildConfigurationView();
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

        makeColumnDropTarget(todoColumn, TaskStatus.TODO);
        makeColumnDropTarget(inProgressColumn, TaskStatus.IN_PROGRESS);
        makeColumnDropTarget(doneColumn, TaskStatus.DONE);
    }

    private void createStatusColumn(Div column, String title, TaskStatus status) {
        column.removeAll();
        column.addClassName("kanban-column");
        column.setWidth("33%");
        column.getElement().setAttribute("status", status.toString());
        column.add(new H3(title));
    }

    private void makeColumnDropTarget(Div column, TaskStatus status) {
        DropTarget.create(column).addDropListener(event -> {
            Optional<Component> dragSourceComponent = event.getDragSourceComponent();
            dragSourceComponent.ifPresent(card -> {
                Div previousParent = (Div) card.getParent().get();
                previousParent.remove(card);

                column.add(card);

                Task task = taskService.getTaskById(card.getId().get());
                task.setStatus(status);
                taskService.updateTask(task);
            });
        });
    }

    private void loadTasks() {
        ArrayList<Task> tasks = taskService.getTasksByProject(project);
        tasks.forEach(this::addTaskToColumn);
    }

    private void addTaskToColumn(Task task) {
        CardComponentDraggable card = new CardComponentDraggable(task.getTitle(), task.getDescription());
        ComponentEventListener<AttachEvent> listener = event -> card.addClickListener(e -> {
            TaskModal taskModal = new TaskModal(task, associationService, taskService);
            taskModal.open();
        });
        card.addAttachListener(listener);
        card.setId(task.getId());

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

    private void buildConfigurationView() {
        configView.removeAll();
        configView.setSizeFull();

        VerticalLayout formLayout = getFormLayout();

        configView.add(formLayout);
    }

    private VerticalLayout getFormLayout() {
        TextField nameField = new TextField("Nom du projet");
        nameField.setValue(project.getName());
        nameField.setWidthFull();

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setValue(project.getDescription());
        descriptionField.setWidthFull();

        HorizontalLayout dateLayout = new HorizontalLayout();
        dateLayout.setWidthFull();

        DatePicker startDatePicker = new DatePicker("Date de début");
        startDatePicker.setValue(project.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        startDatePicker.setWidth("50%");

        DatePicker endDatePicker = new DatePicker("Date de fin");
        endDatePicker.setValue(project.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        endDatePicker.setWidth("50%");

        dateLayout.add(startDatePicker, endDatePicker);

        Button submitButton = new Button("Enregistrer", buttonClickEvent -> {
            String name = nameField.getValue();
            String description = descriptionField.getValue();
            Date startDate = Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            if (!validateFields(nameField, descriptionField, startDatePicker, endDatePicker)) {
                return;
            }

            saveProjectConfiguration(name, description, startDate, endDate);
        });
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();

        return new VerticalLayout(nameField, dateLayout, descriptionField, submitButton);
    }

    private void saveProjectConfiguration(String name, String description, Date startDate, Date endDate) {
        Project project = new Project(
                projectId,
                name,
                description,
                startDate,
                endDate
        );

        Project updatedProject = projectService.updateProject(project);

        if (updatedProject == null) {
            NotificationUtils.createNotification("An error occurred while updating the project", false).open();
        } else {
            NotificationUtils.createNotification("The project has been successfully updated!", true).open();
        }
    }

    private boolean validateFields(TextField titleField, TextArea descriptionField, DatePicker startDateField, DatePicker endDateField) {
        titleField.setInvalid(titleField.isEmpty());
        descriptionField.setInvalid(descriptionField.isEmpty());
        startDateField.setInvalid(startDateField.isEmpty());
        endDateField.setInvalid(endDateField.isEmpty());

        // if start after end date then invalid
        if (!startDateField.isEmpty() && !endDateField.isEmpty() && startDateField.getValue().isAfter(endDateField.getValue())) {
            startDateField.setInvalid(true);
            endDateField.setInvalid(true);
            startDateField.setErrorMessage("La date de début doit être avant la date de fin");
            endDateField.setErrorMessage("La date de fin doit être après la date de début");
            NotificationUtils.createNotification("La date de début doit être avant la date de fin", false).open();
        }

        return !titleField.isInvalid() && !descriptionField.isInvalid() && !startDateField.isInvalid() && !endDateField.isInvalid();
    }
}
