package org.pat.causeconnect.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.entity.User;
import org.pat.causeconnect.service.SecurityService;
import org.pat.causeconnect.service.project.ProjectService;
import org.pat.causeconnect.ui.project.ProjectsView;
import org.pat.causeconnect.ui.task.TasksView;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.ArrayList;
import java.util.Optional;

public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private H2 viewTitle;

    private final SecurityService securityService;
    private final AccessAnnotationChecker accessChecker;

    private User user;

    public MainLayout(SecurityService securityService, AccessAnnotationChecker accessChecker) {
        this.securityService = securityService;
        this.accessChecker = accessChecker;

        Optional<User> userOptional = securityService.getAuthenticatedUser();

        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));
            return;
        }

        setPrimarySection(Section.DRAWER);
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void createDrawer() {
        String associationName = user.getAssociation().getName();
        H1 appName = new H1(associationName);
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        if (accessChecker.hasAccess(DashboardView.class)) {
            nav.addItem(new SideNavItem("Tableau de bord", DashboardView.class, VaadinIcon.HOME_O.create()));
        }

        if (accessChecker.hasAccess(ProjectsView.class)) {
            nav.addItem(new SideNavItem("Mes projets", ProjectsView.class, VaadinIcon.CALENDAR_CLOCK.create()));
        }

        if (accessChecker.hasAccess(TasksView.class)) {
            nav.addItem(new SideNavItem("Mes tâches", TasksView.class, VaadinIcon.BULLETS.create()));
        }

        return nav;
    }

    private Footer createFooter() {
        Footer footer = new Footer();

        if (user != null) {
            Avatar avatar = new Avatar(user.getFullName());
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getFullName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Déconnexion", e -> {
                securityService.logout();
                getUI().ifPresent(ui -> ui.navigate(LoginView.class));
            });

            footer.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
            footer.add(loginLink);
        }

        return footer;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (user == null) {
            beforeEnterEvent.rerouteTo(LoginView.class);
        }
    }
}