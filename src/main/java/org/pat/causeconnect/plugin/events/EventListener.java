package org.pat.causeconnect.plugin.events;

import org.pat.causeconnect.plugin.events.task.CauseConnectEvent;

public interface EventListener<T extends CauseConnectEvent>{
    void onEvent(T event);

}
