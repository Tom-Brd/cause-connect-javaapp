package org.pat.causeconnect.plugin.events;

import jdk.jfr.Event;

public interface EventListener<T extends Event> {
    void onEvent(T event);
}
