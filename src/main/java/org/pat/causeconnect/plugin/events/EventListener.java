package org.pat.causeconnect.plugin.events;

public interface EventListener<T extends CauseConnectEvent>{
    void onEvent(T event);

}
