package org.pat.causeconnect.plugin.events.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.plugin.events.CauseConnectEvent;

@Getter
@AllArgsConstructor
public class GettingSingleTaskEvent extends CauseConnectEvent {
    private Task task;
}
