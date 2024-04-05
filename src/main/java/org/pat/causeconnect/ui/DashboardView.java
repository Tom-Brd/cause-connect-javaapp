package org.pat.causeconnect.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "", layout = MainLayout.class)
@AnonymousAllowed
@PageTitle("Dashboard")
public class DashboardView extends VerticalLayout {
    public DashboardView() {
    }
}
