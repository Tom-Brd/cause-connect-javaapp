package org.pat.causeconnect.plugin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteConfiguration;

public record ViewConfiguration(
        String route,
        Class<? extends Component> viewClass) {

    public void registerViews() {
        System.out.println("In registerViews");
        RouteConfiguration routeConfiguration = RouteConfiguration.forApplicationScope();
        System.out.println("routeConfiguration: " + routeConfiguration);
        routeConfiguration.setAnnotatedRoute(viewClass);
        System.out.println("routeConfiguration UPDATE: " + routeConfiguration);
    }
}
