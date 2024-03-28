package org.pat.causeconnect.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.pat.causeconnect.entity.AssociationContext;
import org.pat.causeconnect.service.SecurityService;
import org.springframework.security.core.userdetails.UserDetails;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Optional;

public class MainLayout extends AppLayout {
    private H2 viewTitle;

    private final SecurityService securityService;
    private final AccessAnnotationChecker accessChecker;

    public MainLayout(SecurityService securityService, AccessAnnotationChecker accessChecker) {
        this.securityService = securityService;
        this.accessChecker = accessChecker;
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
        String associationName = AssociationContext.getInstance().getAssociation().getName();
        H1 appName = new H1(associationName);
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(appName, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        if (accessChecker.hasAccess(DashboardView.class)) {
            nav.addItem(new SideNavItem("Dashboard", DashboardView.class, LineAwesomeIcon.LIST_SOLID.create()));
        }

        return nav;
    }

    private Footer createFooter() {
        Footer footer = new Footer();

        Optional<UserDetails> user = securityService.getAuthenticatedUser();
        if (user.isPresent()) {
            UserDetails userDetails = user.get();

            Avatar avatar = new Avatar(userDetails.getUsername());
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(userDetails.getUsername());
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
}