package org.pat.causeconnect.plugin.events.task;

import jdk.jfr.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.plugin.events.Cancellable;

@Getter
@AllArgsConstructor
public class TaskUpdateEvent extends Event implements Cancellable {
    private Task previousTask;
    private Task newTask;
    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
