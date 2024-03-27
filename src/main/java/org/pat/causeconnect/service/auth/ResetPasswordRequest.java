package org.pat.causeconnect.service.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResetPasswordRequest {
    private String email;
    private String associationId;
}
