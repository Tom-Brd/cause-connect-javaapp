package org.pat.causeconnect.service.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserDetailResponse {
    private String id;
    private String email;
    private String fullName;
    private String role;
}
