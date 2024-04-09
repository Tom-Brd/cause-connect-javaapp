package org.pat.causeconnect.ui.component;

import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class CardComponentDraggable extends CardComponent implements DragSource<CardComponentDraggable> {
    public CardComponentDraggable(String title, String description) {
        super(title, description);
        setDraggable(true);
    }
}
