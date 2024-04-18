package org.pat.causeconnect.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;
    private String email;
    private String fullName;
    private List<GrantedAuthority> role;
    private Association association;

    public boolean isAdmin() {
        return role.stream().anyMatch(r -> r.getAuthority().equals("USER_ADMIN"));
    }
}
