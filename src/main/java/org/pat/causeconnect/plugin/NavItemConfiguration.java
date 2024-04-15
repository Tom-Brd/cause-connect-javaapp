package org.pat.causeconnect.plugin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import org.pat.causeconnect.ui.MainLayout;

public record NavItemConfiguration(
        String title,
        Class<? extends Component> viewClass,
        VaadinIcon icon
) {
    public void addNavItem(AccessAnnotationChecker accessAnnotationChecker) {
        System.out.println("Adding nav item");
        UI.getCurrent().access(() -> {
            System.out.println("in the UI.getCurrent");
            MainLayout mainLayout = (MainLayout) UI.getCurrent().getChildren()
                    .filter(component -> component instanceof MainLayout)
                    .findFirst()
                    .orElse(null);

            if (mainLayout != null) {
                SideNav nav= mainLayout.getNavigation();
                if (accessAnnotationChecker.hasAccess(viewClass)) {
                    nav.addItem(new SideNavItem(title, viewClass, icon.create()));
                }
            }
        });

    }
}
