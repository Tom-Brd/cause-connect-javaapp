package org.pat.causeconnect.service.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.pat.causeconnect.entity.Project;

@Getter
@Setter
@AllArgsConstructor
public class ProjectByIdResponse {
    private Project project;
}
