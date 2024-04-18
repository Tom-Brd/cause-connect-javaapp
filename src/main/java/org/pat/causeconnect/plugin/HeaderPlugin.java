package org.pat.causeconnect.plugin;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public interface HeaderPlugin extends CauseConnectPlugin {
    void addToHeader(HorizontalLayout header);
}
