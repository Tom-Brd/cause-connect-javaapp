package org.pat.causeconnect.plugin.events.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.entity.task.TaskStatus;
import org.pat.causeconnect.plugin.events.Cancellable;
import org.pat.causeconnect.plugin.events.CauseConnectEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class TaskMoveEvent extends CauseConnectEvent implements Cancellable {
    private Task task;
    private TaskStatus newStatus;
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
