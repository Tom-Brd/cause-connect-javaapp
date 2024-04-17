package org.pat.causeconnect.ui.task;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.entity.User;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.entity.task.TaskStatus;
import org.pat.causeconnect.plugin.events.EventManager;
import org.pat.causeconnect.plugin.events.task.TaskUpdateEvent;
import org.pat.causeconnect.service.AssociationService;
import org.pat.causeconnect.service.task.TaskService;
import org.pat.causeconnect.ui.utils.NotificationUtils;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;

public class TaskModal extends Dialog {

    private final boolean isEditMode;
    private Task task;

    public TaskModal(Project project, AssociationService associationService, TaskService taskService, EventManager eventManager) {
        this.task = new Task();
        this.task.setProject(project);
        this.isEditMode = false;
        buildLayout(associationService, taskService, eventManager, project);
    }

    public TaskModal(Task task, AssociationService associationService, TaskService taskService, EventManager eventManager) {
        this.task = task;
        this.isEditMode = true;
        buildLayout(associationService, taskService, eventManager, task.getProject());
    }

    private void buildLayout(AssociationService associationService, TaskService taskService, EventManager eventManager, Project project) {
        setModal(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setWidth("60%");

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        H3 title = new H3(isEditMode ?
                "Modifier la tâche pour le projet " + task.getProject().getName() :
                "Créer une nouvelle tâche pour le projet " + project.getName());

        HorizontalLayout titleAndUser = new HorizontalLayout();
        titleAndUser.setWidth("100%");
        titleAndUser.setPadding(false);
        titleAndUser.setSpacing(true);

        TextField titleField = new TextField("Titre");
        titleField.setValue(task.getTitle() != null ? task.getTitle() : "");
        titleField.setClearButtonVisible(true);
        titleField.setRequiredIndicatorVisible(true);
        titleField.setWidth("50%");

        ArrayList<User> members = associationService.getMembers();
        ComboBox<String> userComboBox = new ComboBox<>("Assignée à");
        userComboBox.setItems(members.stream().map(User::getFullName).toList());
        userComboBox.setValue(task.getResponsibleUser() != null ? task.getResponsibleUser().getFullName() : null);
        userComboBox.setWidth("50%");
        titleAndUser.add(titleField, userComboBox);

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setValue(task.getDescription() != null ? task.getDescription() : "");
        descriptionField.setWidthFull();
        descriptionField.setRequired(true);

        DateTimePicker dateTimePicker = new DateTimePicker("Échéance");
        dateTimePicker.setValue(task.getDeadline() != null ? task.getDeadline().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        dateTimePicker.setWidth("100%");

        ComboBox<String> statusComboBox = new ComboBox<>("Statut");
        statusComboBox.setItems(Arrays.stream(TaskStatus.values()).map(Enum::name).toList());
        statusComboBox.setValue(task.getStatus() != null ? task.getStatus().name() : TaskStatus.TODO.name());
        statusComboBox.setWidth("25%");
        if (!isEditMode) {
            statusComboBox.setReadOnly(true);
        }

        Button saveButton = new Button(isEditMode ? "Enregistrer" : "Créer", e -> {
            if (validateFields(titleField, descriptionField, dateTimePicker, statusComboBox)) {
                Task tempTask = new Task();

                tempTask.setId(task.getId());
                tempTask.setTitle(titleField.getValue());
                tempTask.setDescription(descriptionField.getValue());
                tempTask.setDeadline(java.util.Date.from(dateTimePicker.getValue().atZone(ZoneId.systemDefault()).toInstant()));
                tempTask.setResponsibleUser(members.stream().filter(user -> user.getFullName().equals(userComboBox.getValue())).findFirst().orElse(null));
                tempTask.setProject(project);

                if (isEditMode) {
                    tempTask.setStatus(TaskStatus.valueOf(statusComboBox.getValue()));
                    TaskUpdateEvent taskUpdateEvent = new TaskUpdateEvent(task, tempTask, false);
                    eventManager.fireEvent(taskUpdateEvent);

                    if (taskUpdateEvent.isCancelled()) {
                        return;
                    }

                    Task updatedTask = taskService.updateTask(tempTask);
                    if (updatedTask == null) {
                        return;
                    }
                    task = updatedTask;
                } else {
                    System.out.println("Creating task");
                    Task createdTask = taskService.createTask(tempTask);
                    if (createdTask == null) {
                        return;
                    }
                    System.out.println(tempTask.getResponsibleUser());
                    if (tempTask.getResponsibleUser() != null) {
                        System.out.println("Assigning task to user");
                        taskService.assignTask(createdTask, tempTask.getResponsibleUser());
                        createdTask.setResponsibleUser(tempTask.getResponsibleUser());
                    }
                    task = createdTask;
                    close();
                }
            }

            NotificationUtils.createNotification("Modification appliquées !", true).open();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        content.add(title, titleAndUser, statusComboBox, dateTimePicker, descriptionField, saveButton);
        add(content);
    }

    private boolean validateFields(TextField titleField, TextArea descriptionField, DateTimePicker dateTimePicker, ComboBox<String> statusComboBox) {
        titleField.setInvalid(titleField.isEmpty());
        descriptionField.setInvalid(descriptionField.isEmpty());
        dateTimePicker.setInvalid(dateTimePicker.isEmpty());
        statusComboBox.setInvalid(statusComboBox.isEmpty());

        return !titleField.isEmpty() && !descriptionField.isEmpty() && !dateTimePicker.isEmpty() && !statusComboBox.isEmpty();
    }
}
