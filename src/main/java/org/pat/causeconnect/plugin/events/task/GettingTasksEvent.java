package org.pat.causeconnect.plugin.events.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pat.causeconnect.entity.task.Task;
import org.pat.causeconnect.plugin.events.CauseConnectEvent;

import java.util.ArrayList;

@Getter
@AllArgsConstructor
public class GettingTasksEvent extends CauseConnectEvent {
    private ArrayList<Task> tasks;
}
