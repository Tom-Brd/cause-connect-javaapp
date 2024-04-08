package org.pat.causeconnect.service.task;

import lombok.Getter;
import lombok.Setter;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.service.user.UserDetailResponse;

import java.util.Date;

@Getter
@Setter
public class TaskResponse {
    private String id;
    private String title;
    private String description;
    private String status;
    private Date deadline;
    private UserDetailResponse responsibleUser;
    private Project project;
}
