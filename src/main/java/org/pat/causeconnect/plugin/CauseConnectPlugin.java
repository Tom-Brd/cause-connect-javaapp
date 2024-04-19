package org.pat.causeconnect.plugin;

import org.pat.causeconnect.plugin.events.EventManager;
import org.pat.causeconnect.service.MainLayoutService;

import java.util.Collection;
import java.util.Collections;

public interface CauseConnectPlugin {
    void load(EventManager eventManager, MainLayoutService mainLayoutService);

    default void unload() {
    }

    default Collection<ViewConfiguration> getViews() {
        return Collections.emptyList();
    }

    default Collection<NavItemConfiguration> getNavItems() {
        return Collections.emptyList();
    }
}
