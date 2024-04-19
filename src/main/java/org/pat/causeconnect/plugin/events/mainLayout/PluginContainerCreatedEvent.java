package org.pat.causeconnect.plugin.events.mainLayout;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.pat.causeconnect.plugin.events.CauseConnectEvent;

public class PluginContainerCreatedEvent extends CauseConnectEvent {
    private final HorizontalLayout pluginContainer;

    public PluginContainerCreatedEvent(HorizontalLayout pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    public HorizontalLayout getPluginContainer() {
        return pluginContainer;
    }
}
