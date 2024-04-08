package org.pat.causeconnect.ui.project;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.service.project.ProjectService;
import org.pat.causeconnect.ui.utils.NotificationUtils;

import java.time.ZoneId;
import java.util.Date;

public class ProjectModal extends Dialog {
    public ProjectModal(ProjectService projectService) {
        setModal(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);
        setWidth("60%");

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();

        H3 title = new H3("Créer un nouveau projet");

        TextField titleField = new TextField("Nom du projet");
        titleField.setClearButtonVisible(true);
        titleField.setRequiredIndicatorVisible(true);
        titleField.setWidthFull();

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();
        descriptionField.setRequired(true);

        HorizontalLayout datesLayout = new HorizontalLayout();
        datesLayout.setWidthFull();

        DatePicker startDateField = new DatePicker("Date de début");
        startDateField.setRequired(true);
        startDateField.setWidth("50%");

        DatePicker endDateField = new DatePicker("Date de fin");
        endDateField.setRequired(true);
        endDateField.setWidth("50%");

        datesLayout.add(startDateField, endDateField);

        Button saveButton = new Button("Créer", e -> {
            if (validateFields(titleField, descriptionField, startDateField, endDateField)) {
                Project project = new Project();
                project.setName(titleField.getValue());
                project.setDescription(descriptionField.getValue());
                project.setStartTime(Date.from(startDateField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                project.setEndTime(Date.from(endDateField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));

                Project projectCreated = projectService.createProject(project);
                if (projectCreated == null) {
                    NotificationUtils.createNotification("Une erreur est survenue lors de la création du projet", false).open();
                } else {
                    NotificationUtils.createNotification("Le projet a bien été créé !", true).open();
                }

                close();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        content.add(title, titleField, descriptionField, datesLayout, saveButton);
        add(content);
}

private boolean validateFields(TextField titleField, TextArea descriptionField, DatePicker startDateField, DatePicker endDateField) {
    titleField.setInvalid(titleField.isEmpty());
    descriptionField.setInvalid(descriptionField.isEmpty());
    startDateField.setInvalid(startDateField.isEmpty());
    endDateField.setInvalid(endDateField.isEmpty());

    // if start after end date then invalid
    if (startDateField.getValue().isAfter(endDateField.getValue())) {
        startDateField.setInvalid(true);
        endDateField.setInvalid(true);
        startDateField.setErrorMessage("La date de début doit être avant la date de fin");
        endDateField.setErrorMessage("La date de fin doit être après la date de début");
        NotificationUtils.createNotification("La date de début doit être avant la date de fin", false).open();
    }

    return !titleField.isInvalid() && !descriptionField.isInvalid() && !startDateField.isInvalid() && !endDateField.isInvalid();
}
}
