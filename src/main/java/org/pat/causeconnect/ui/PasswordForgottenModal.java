package org.pat.causeconnect.ui;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import org.pat.causeconnect.service.auth.AuthenticationService;

import static org.pat.causeconnect.ui.utils.NotificationUtils.createNotification;

public class PasswordForgottenModal extends Dialog {
    public PasswordForgottenModal(AuthenticationService authenticationService) {
        setModal(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(true);

        setWidth("600px");
        setHeight("350px");

        H2 title = new H2("Réinitialisation de mot de passe");
        Paragraph description = new Paragraph("Veuillez saisir votre email pour réinitialiser votre mot de passe.");

        EmailField emailField = getEmailField();
        Button sendButton = getButton(authenticationService, emailField);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        dialogLayout.add(title, description, emailField, sendButton);
        add(dialogLayout);
    }

    private static EmailField getEmailField() {
        EmailField emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setClearButtonVisible(true);
        emailField.setPlaceholder("example@domain.com");
        emailField.setRequired(true);
        emailField.setErrorMessage("Veuillez saisir un email valide.");

        return emailField;
    }

    private Button getButton(AuthenticationService authenticationService, EmailField emailField) {
        Button sendButton = new Button("Envoyer", e -> {
            try {
                authenticationService.resetPassword(emailField.getValue());
                Notification notification = createNotification(
                        "Un email de réinitialisation de mot de passe a été envoyé.",
                        true
                );
                notification.open();
                close();
            } catch (Exception ex) {
                Notification notification = createNotification(
                        "Erreur lors de l'envoi de l'email de réinitialisation de mot de passe.",
                        false
                );
                notification.open();
            }
        });
        sendButton.setWidthFull();
        sendButton.setThemeName("primary");
        sendButton.addClickShortcut(Key.ENTER);
        return sendButton;
    }
}
