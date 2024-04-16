package org.pat.causeconnect.plugin.events.task;

import jdk.jfr.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pat.causeconnect.entity.task.Task;

@Getter
@AllArgsConstructor
public class TaskUpdateEvent extends Event {
    private Task previousTask;
    private Task newTask;
}
