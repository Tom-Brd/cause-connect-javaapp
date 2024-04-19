package org.pat.causeconnect.ui;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.pat.causeconnect.entity.Association;
import org.pat.causeconnect.entity.AssociationContext;
import org.pat.causeconnect.service.AssociationService;
import org.pat.causeconnect.service.InternetCheckService;
import org.pat.causeconnect.service.SecurityService;
import org.pat.causeconnect.service.auth.AuthenticationService;
import org.pat.causeconnect.ui.utils.NotificationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final InternetCheckService internetCheckService;
    private AtomicBoolean isValid;

    public LoginView(AuthenticationService authenticationService, AssociationService associationService, SecurityService securityService, InternetCheckService internetCheckService) throws IOException {
        this.internetCheckService = internetCheckService;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Resource resource = new ClassPathResource("static/cause-connector.png");
        InputStream inputStream = resource.getInputStream();
        Image logo = new Image(new StreamResource("cause-connector.png", () -> inputStream), "cause-connect-logo");
        logo.setWidth("200px");
        logo.setHeight("200px");
        logo.getStyle().set("object-fit", "contain");

        Select<Association> associationSelect = createAssociationSelect(associationService, logo);
        EmailField emailField = createEmailField();
        PasswordField passwordField = createPasswordField();
        Button loginButton = createButtonLogin(authenticationService, emailField, passwordField, associationSelect);
        Button forgotPassword = createForgotPasswordButton(authenticationService);

        passwordField.addKeyDownListener(event -> {
            if (event.getKey().equals(Key.ENTER)) {
                login(authenticationService, emailField, passwordField, associationSelect);
            }
        });
        emailField.addKeyDownListener(event -> {
            if (event.getKey().equals(Key.ENTER)) {
                login(authenticationService, emailField, passwordField, associationSelect);
            }
        });

        Button closeButton = new Button("Quitter", e -> securityService.shutdown());
        closeButton.setIcon(VaadinIcon.ARROW_RIGHT.create());
        closeButton.setIconAfterText(true);
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClassName("leave-button");

        add(logo, associationSelect, emailField, passwordField, loginButton, forgotPassword, closeButton);
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
        isValid = new AtomicBoolean(true);

        loginButton.addClickListener(e -> {
            login(authenticationService, emailField, passwordField, associationSelect);
        });
        return loginButton;
    }

    private void login(AuthenticationService authenticationService, EmailField emailField, PasswordField passwordField, Select<Association> associationSelect) {
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

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (VaadinSession.getCurrent().getAttribute(Authentication.class) != null) {
            beforeEnterEvent.forwardTo("");
            return;
        }

        if (!internetCheckService.hasInternetConnection()) {
            NotificationUtils.createNotification("Pas de connexion Internet, veuillez vérifier votre connexion.", false).open();
        }
    }
}
