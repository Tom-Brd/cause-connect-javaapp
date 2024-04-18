package org.pat.causeconnect.plugin.events;

public interface Cancellable {
    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
