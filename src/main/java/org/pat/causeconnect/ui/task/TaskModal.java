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
import org.pat.causeconnect.entity.User;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.entity.task.TaskStatus;
import org.pat.causeconnect.service.AssociationService;
import org.pat.causeconnect.service.task.TaskService;
import org.pat.causeconnect.ui.utils.NotificationUtils;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;

public class TaskModal extends Dialog {

    public TaskModal(Task task, AssociationService associationService, TaskService taskService) {
        setModal(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setWidth("60%");

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        H3 title = new H3("Modifier la tâche");

        HorizontalLayout titleAndUser = new HorizontalLayout();
        titleAndUser.setWidth("100%");
        titleAndUser.setPadding(false);
        titleAndUser.setSpacing(true);

        TextField titleField = new TextField("Titre");
        titleField.setValue(task.getTitle());
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
        descriptionField.setValue(task.getDescription());
        descriptionField.setWidthFull();
        descriptionField.setRequired(true);

        DateTimePicker dateTimePicker = new DateTimePicker("Échéance");
        dateTimePicker.setValue(task.getDeadline() != null ? task.getDeadline().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        dateTimePicker.setWidth("100%");

        ComboBox<String> statusComboBox = new ComboBox<>("Statut");
        statusComboBox.setItems(Arrays.stream(TaskStatus.values()).map(Enum::name).toList());
        statusComboBox.setValue(task.getStatus() != null ? task.getStatus().name() : TaskStatus.TODO.name());
        statusComboBox.setWidth("25%");

        Button saveButton = new Button("Enregistrer", e -> {
            if (validateFields(titleField, userComboBox, descriptionField, dateTimePicker, statusComboBox)) {
                task.setTitle(titleField.getValue());
                task.setDescription(descriptionField.getValue());
                task.setDeadline(java.util.Date.from(dateTimePicker.getValue().atZone(ZoneId.systemDefault()).toInstant()));
                task.setStatus(TaskStatus.valueOf(statusComboBox.getValue()));
                task.setResponsibleUser(members.stream().filter(user -> user.getFullName().equals(userComboBox.getValue())).findFirst().orElse(null));

                Task updatedTask = taskService.updateTask(task);
                // navigate to the same page to refresh the view
                UI.getCurrent().navigate(TasksView.class);
                if (updatedTask == null) {
                    NotificationUtils.createNotification("Erreur lors de la sauvegarde des modifications", false).open();
                    return;
                }
            }

            NotificationUtils.createNotification("Modification appliquées !", true).open();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        content.add(title, titleAndUser, statusComboBox, dateTimePicker, descriptionField, saveButton);
        add(content);
    }

    private boolean validateFields(TextField titleField, ComboBox<String> userComboBox, TextArea descriptionField, DateTimePicker dateTimePicker, ComboBox<String> statusComboBox) {
        titleField.setInvalid(titleField.isEmpty());
        userComboBox.setInvalid(userComboBox.isEmpty());
        descriptionField.setInvalid(descriptionField.isEmpty());
        dateTimePicker.setInvalid(dateTimePicker.isEmpty());
        statusComboBox.setInvalid(statusComboBox.isEmpty());

        return !titleField.isEmpty() && !userComboBox.isEmpty() && !descriptionField.isEmpty() && !dateTimePicker.isEmpty() && !statusComboBox.isEmpty();
    }
}
