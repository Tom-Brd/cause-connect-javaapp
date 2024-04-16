package org.pat.causeconnect.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.pat.causeconnect.entity.Theme;
import org.pat.causeconnect.entity.User;
import org.pat.causeconnect.plugin.HeaderPlugin;
import org.pat.causeconnect.plugin.NavItemConfiguration;
import org.pat.causeconnect.plugin.PluginLoader;
import org.pat.causeconnect.service.SecurityService;
import org.pat.causeconnect.service.theme.ThemeService;
import org.pat.causeconnect.ui.project.ProjectsView;
import org.pat.causeconnect.ui.task.TasksView;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private H2 viewTitle;
    private final SideNav sideNav;

    private HorizontalLayout pluginContainer;

    private final SecurityService securityService;
    private final AccessAnnotationChecker accessChecker;
    private final PluginLoader pluginLoader;

    private User user;

    public MainLayout(SecurityService securityService, AccessAnnotationChecker accessChecker, ThemeService themeService, PluginLoader pluginLoader) {
        this.securityService = securityService;
        this.accessChecker = accessChecker;
        this.pluginLoader = pluginLoader;

        this.sideNav = createNavigation();

        Optional<User> userOptional = securityService.getAuthenticatedUser();

        if (userOptional.isPresent()) {
            user = userOptional.get();
            VaadinSession.getCurrent().setAttribute(User.class, user);
        } else {
            securityService.logout();
            UI.getCurrent().navigate(LoginView.class);
            return;
        }

        setPrimarySection(Section.DRAWER);
        setTheme(themeService.getTheme());
        createHeader();
        loadHeaderPlugins();
        createDrawer();
    }

    private void setTheme(Theme theme) {
        String primaryColor = theme.getColor();
        String primaryColor50pct = theme.getColor50pct();
        String primaryColor10pct = theme.getColor10pct();

        UI.getCurrent().getStyle().set("--lumo-primary-color", primaryColor);
        UI.getCurrent().getStyle().set("--lumo-primary-color-50pct", primaryColor50pct);
        UI.getCurrent().getStyle().set("--lumo-primary-color-10pct", primaryColor10pct);

        String font = theme.getFont();
        UI.getCurrent().getStyle().set("--lumo-font-family", font);
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        viewTitle.getStyle().set("white-space", "nowrap");

        pluginContainer = new HorizontalLayout();
        pluginContainer.setWidthFull();
        pluginContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        pluginContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER );
        pluginContainer.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Button closeButton = new Button("Quitter", e -> securityService.shutdown());
        closeButton.setIcon(VaadinIcon.ARROW_RIGHT.create());
        closeButton.setIconAfterText(true);
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClassName("leave-button");

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        headerLayout.add(toggle, viewTitle, pluginContainer, closeButton);

        addToNavbar(true, headerLayout);
    }

    private void loadHeaderPlugins() {
        List<HeaderPlugin> headerPlugins = pluginLoader.getAllHeaderPlugins();
        for (HeaderPlugin plugin : headerPlugins) {
            plugin.addToHeader(pluginContainer);
        }
    }

    private void createDrawer() {
        String associationName = user.getAssociation().getName();
        H1 appName = new H1(associationName);
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        List<NavItemConfiguration> navItems = pluginLoader.getAllNavItems();
        for (NavItemConfiguration navItem : navItems) {
            if (accessChecker.hasAccess(navItem.viewClass())) {
                System.out.println("Adding nav item");
                sideNav.addItem(new SideNavItem(navItem.title(), navItem.viewClass(), navItem.icon().create()));
            }
        }

        Scroller scroller = new Scroller(sideNav);
        System.out.println("je reconstruis le drawer");

        addToDrawer(header, scroller, createFooter());
    }

    public SideNav getNavigation() {
        return sideNav;
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
    public void beforeEnter(BeforeEnterEvent event) {
        if (VaadinSession.getCurrent().getAttribute(Authentication.class) == null) {
            event.forwardToUrl("login");
        }
    }
}