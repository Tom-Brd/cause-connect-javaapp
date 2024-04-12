package org.pat.causeconnect.service.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.pat.causeconnect.entity.Project;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
public class MyProjectsResponse {
    private ArrayList<Project> myProjects;
}
