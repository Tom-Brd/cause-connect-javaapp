package org.pat.causeconnect.ui.utils;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class NotificationUtils {
    public static Notification createNotification(String text, boolean isSuccess) {
        Notification notification = new Notification();
        notification.setPosition(Notification.Position.TOP_CENTER);
        Icon icon = isSuccess ? VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.WARNING.create();

        HorizontalLayout layout = new HorizontalLayout(
                icon,
                new Text(text)
        );
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        notification.add(layout);

        notification.addThemeVariants(isSuccess ?
                NotificationVariant.LUMO_SUCCESS :
                NotificationVariant.LUMO_ERROR);
        notification.setDuration(5000);
        return notification;
    }
}
