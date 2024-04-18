package org.pat.causeconnect.plugin;

import org.pat.causeconnect.plugin.events.EventManager;

import java.util.Collection;
import java.util.Collections;

public interface CauseConnectPlugin {
    void load(EventManager eventManager);

    default void unload() {
    }

    default Collection<ViewConfiguration> getViews() {
        return Collections.emptyList();
    }

    default Collection<NavItemConfiguration> getNavItems() {
        return Collections.emptyList();
    }
}
