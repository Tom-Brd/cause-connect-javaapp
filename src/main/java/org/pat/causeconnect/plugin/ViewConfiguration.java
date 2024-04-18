package org.pat.causeconnect.plugin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteConfiguration;

public record ViewConfiguration(
        String route,
        Class<? extends Component> viewClass) {

    public void registerViews() {
        RouteConfiguration routeConfiguration = RouteConfiguration.forApplicationScope();
        routeConfiguration.setAnnotatedRoute(viewClass);
    }

    public void unregisterViews() {
        RouteConfiguration routeConfiguration = RouteConfiguration.forApplicationScope();
        routeConfiguration.removeRoute(route);
    }
}
