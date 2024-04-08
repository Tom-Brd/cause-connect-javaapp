package org.pat.causeconnect.service.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TaskUpdateRequest {
    private String title;
    private String description;
    private String status;
    private String deadline;
    private String projectId;
    private String responsibleUserId;
}
