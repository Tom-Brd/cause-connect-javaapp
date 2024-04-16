package org.pat.causeconnect.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Plugin {
    private String id;
    private String name;
    private String description;
    private String author;
}
