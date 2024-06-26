package org.pat.causeconnect.plugin.events.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.plugin.events.Cancellable;
import org.pat.causeconnect.plugin.events.CauseConnectEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class TaskUpdateEvent extends CauseConnectEvent implements Cancellable {
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
