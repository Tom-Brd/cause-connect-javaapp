package org.pat.causeconnect.plugin;

import java.util.Collection;
import java.util.Collections;

public interface CauseConnectPlugin {
    void load();

    default Collection<ViewConfiguration> getViews() {
        return Collections.emptyList();
    }

    default Collection<NavItemConfiguration> getNavItems() {
        return Collections.emptyList();
    }
}
