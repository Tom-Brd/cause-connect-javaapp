package org.pat.causeconnect.entity.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.pat.causeconnect.entity.Project;
import org.pat.causeconnect.entity.User;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    private String id;
    private String title;
    private String description;
    private TaskStatus status;
    private Date deadline;
    private User responsibleUser;
    private Project project;
}
