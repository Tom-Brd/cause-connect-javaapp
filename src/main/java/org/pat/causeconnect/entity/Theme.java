package org.pat.causeconnect.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Theme {
    private String id;
    private String color;
    private String font;

    public String getColor50pct() {
        return color + "80";
    }

    public String getColor10pct() {
        return color + "1A";
    }
}
