package org.pat.causeconnect.service.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.pat.causeconnect.entity.task.Task;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
public class MyTasksResponse {
    private ArrayList<Task> taskResponses;
}
