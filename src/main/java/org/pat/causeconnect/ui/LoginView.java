package org.pat.causeconnect.ui;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.pat.causeconnect.entity.AssociationContext;
import org.pat.causeconnect.service.auth.AuthenticationService;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final LoginForm loginForm = new LoginForm();

    public LoginView(AuthenticationService authenticationService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        Image logo = new Image(AssociationContext.getInstance().getAssociation().getLogo(), "logo");
        logo.setWidth("100px");
        logo.setHeight("100px");

        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setTitle(null);
        i18nForm.setUsername("Email");

        i18nForm.setPassword("Mot de passe");
        i18nForm.setSubmit("Connexion");
        i18nForm.setForgotPassword("Mot de passe oublié");
        i18n.setForm(i18nForm);

        // change error message
        i18n.getErrorMessage().setTitle("Email ou mot de passe incorrect");
        i18n.getErrorMessage().setMessage("Veuillez vérifier votre email et mot de passe puis réessayer.");

        loginForm.addForgotPasswordListener(e -> new PasswordForgottenModal(authenticationService).open());

        loginForm.setAction("login");
        loginForm.setI18n(i18n);

        add(
                logo,
                loginForm
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }

}
