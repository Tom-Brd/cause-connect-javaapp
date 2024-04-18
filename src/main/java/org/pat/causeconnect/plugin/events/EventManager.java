package org.pat.causeconnect.plugin.events;

import org.pat.causeconnect.plugin.CauseConnectPlugin;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventManager {
    private final Map<Class<? extends CauseConnectEvent>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();
    private final Map<CauseConnectPlugin, List<EventListener<?>>> eventsListened= new ConcurrentHashMap<>();

    public <T extends CauseConnectEvent> void registerListener(Class<T> eventClass, EventListener<T> listener, CauseConnectPlugin plugin) {
        listeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
        eventsListened.computeIfAbsent(plugin, k -> new ArrayList<>()).add(listener);
        System.out.println("Registered listener for " + eventClass + " : " + listener.getClass());
    }

    public <T extends CauseConnectEvent> void fireEvent(T event) {
        List<EventListener<?>> eventListeners = listeners.getOrDefault(event.getClass(), Collections.emptyList());
        System.out.println("Firing event for " + event.getClass() + " to " + eventListeners.size() + " listeners.");
        for (EventListener<?> listener : eventListeners) {
            ((EventListener<T>)listener).onEvent(event);
        }
    }

    public <T extends CauseConnectEvent> void unregisterListener(CauseConnectPlugin plugin) {
        eventsListened.get(plugin).forEach(listener -> {
            for(Map.Entry<Class<? extends CauseConnectEvent>, List<EventListener<?>>> entry : listeners.entrySet()) {
                entry.getValue().removeIf(eventListener -> eventListener.equals(listener));
            }
        });
        eventsListened.remove(plugin);
    }
}
