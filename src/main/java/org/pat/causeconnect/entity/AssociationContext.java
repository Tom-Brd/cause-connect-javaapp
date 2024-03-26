package org.pat.causeconnect.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssociationContext {
    private static AssociationContext associationContext;
    private Association association;

    public static AssociationContext getInstance() {
        if (associationContext == null) {
            associationContext = new AssociationContext();
        }
        return associationContext;
    }
}
