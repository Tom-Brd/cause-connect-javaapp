package org.pat.causeconnect.entity;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Project {
    private String id;
    private String name;
    private String description;
    private Date startTime;
    private Date endTime;
}
