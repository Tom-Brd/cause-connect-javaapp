package org.pat.causeconnect.plugin.events;

import jdk.jfr.Event;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventManager {
    private final Map<Class<? extends Event>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    public <T extends Event> void registerListener(Class<T> eventClass, EventListener<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
        System.out.println("Registered listener for " + eventClass + " : " + listener.getClass());
    }

    public <T extends Event> void fireEvent(T event) {
        List<EventListener<?>> eventListeners = listeners.getOrDefault(event.getClass(), Collections.emptyList());
        System.out.println("Firing event for " + event.getClass() + " to " + eventListeners.size() + " listeners.");
        for (EventListener<?> listener : eventListeners) {
            ((EventListener<T>) listener).onEvent(event);
        }
    }
}
