package org.pat.causeconnect.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.pat.causeconnect.entity.Association;
import org.pat.causeconnect.entity.AssociationContext;
import org.pat.causeconnect.service.AssociationService;
import org.pat.causeconnect.service.auth.AuthenticationService;
import org.pat.causeconnect.ui.utils.NotificationUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.concurrent.atomic.AtomicBoolean;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    public LoginView(AuthenticationService authenticationService, AssociationService associationService) {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Image logo = new Image("https://media.discordapp.net/attachments/1209450636890873857/1220849023494131832/image.png?ex=6619a9c1&is=660734c1&hm=252780c33b6ede9746e6b98f81dca766f6279a95c039968342b837d6ae71ca03&format=webp&quality=lossless&width=823&height=673&", "logo");
        logo.setWidth("200px");
        logo.setHeight("200px");

        Select<Association> associationSelect = createAssociationSelect(associationService, logo);
        EmailField emailField = createEmailField();
        PasswordField passwordField = createPasswordField();
        Button loginButton = createButtonLogin(authenticationService, emailField, passwordField, associationSelect);
        Button forgotPassword = createForgotPasswordButton(authenticationService);

        add(logo, associationSelect, emailField, passwordField, loginButton, forgotPassword);
    }

    private Button createForgotPasswordButton(AuthenticationService authenticationService) {
        Button forgotPassword = new Button("Mot de passe oublié");
        forgotPassword.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        forgotPassword.setWidth("300px");
        forgotPassword.addClickListener(e -> {
            PasswordForgottenModal passwordForgottenModal = new PasswordForgottenModal(authenticationService);
            passwordForgottenModal.open();
        });
        return forgotPassword;
    }

    private Button createButtonLogin(AuthenticationService authenticationService, EmailField emailField, PasswordField passwordField, Select<Association> associationSelect) {
        Button loginButton = new Button("Connexion");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidth("300px");
        AtomicBoolean isValid = new AtomicBoolean(true);

        loginButton.addClickListener(e -> {
            emailField.setInvalid(false);
            passwordField.setInvalid(false);
            associationSelect.setInvalid(false);
            isValid.set(true);

            if (emailField.isEmpty()) {
                emailField.setInvalid(true);
                isValid.set(false);
            }
            if (passwordField.isEmpty()) {
                passwordField.setInvalid(true);
                isValid.set(false);
            } else if (passwordField.getValue().length() < 8) {
                passwordField.setInvalid(true);
                passwordField.setErrorMessage("Le mot de passe doit contenir au moins 8 caractères.");
                isValid.set(false);
            }
            if (associationSelect.isEmpty()) {
                associationSelect.setInvalid(true);
                isValid.set(false);
            }

            if (isValid.get()) {
                try {
                    Authentication authentication = authenticationService.authenticate(emailField.getValue(), passwordField.getValue(), associationSelect.getValue().getId());
                    VaadinSession.getCurrent().setAttribute(Authentication.class, authentication);
                    UI.getCurrent().navigate("");
                } catch (BadCredentialsException ex) {
                    emailField.setErrorMessage("Email ou mot de passe incorrect.");
                    emailField.setInvalid(true);
                    passwordField.setInvalid(true);
                    NotificationUtils.createNotification("Email ou mot de passe incorrect.", false).open();
                    isValid.set(false);
                }
            }
        });
        return loginButton;
    }

    private PasswordField createPasswordField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Password");
        passwordField.setRequired(true);
        passwordField.setWidth("300px");
        passwordField.setMinLength(8);
        passwordField.setErrorMessage("Veuillez saisir un mot de passe.");
        return passwordField;
    }

    private Select<Association> createAssociationSelect(AssociationService associationService, Image logo) {
        Association[] associations = associationService.getAssociations();
        Select<Association> associationSelect = new Select<>();

        if (associations != null) {
            associationSelect.setItems(associations);
            associationSelect.setItemLabelGenerator(Association::getName);
            associationSelect.addValueChangeListener(e -> {
                AssociationContext.getInstance().setAssociation(e.getValue());
                VaadinSession.getCurrent().setAttribute("association", e.getValue());
                logo.setSrc(e.getValue().getLogo());
            });
        } else {
            NotificationUtils.createNotification("Aucune association n'a été trouvée.", false).open();
        }
        associationSelect.setWidth("300px");
        associationSelect.setLabel("Association");

        return associationSelect;
    }

    private EmailField createEmailField() {
        EmailField emailField = new EmailField();
        emailField.setLabel("Email");
        emailField.setRequired(true);
        emailField.setWidth("300px");
        emailField.setErrorMessage("Veuillez saisir un email valide.");
        emailField.setPlaceholder("example@example.example");
        return emailField;
    }
}
