package org.pat.causeconnect.service;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class MainLayoutService {
    private HorizontalLayout pluginContainer;

    public void setPluginContainer(HorizontalLayout pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    public HorizontalLayout getPluginContainer() {
        return pluginContainer;
    }
}
