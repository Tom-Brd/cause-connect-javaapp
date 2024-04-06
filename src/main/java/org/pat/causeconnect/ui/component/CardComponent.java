package org.pat.causeconnect.ui.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class CardComponent extends Div {
    private final String title;
    private final String description;

    public CardComponent(String title, String description) {
        this.title = title;
        this.description = description;
        buildCard();
    }

    private void buildCard() {
        addClassName("card");
        Span titleText = new Span(title);
        titleText.addClassName("card--title");

        Span descriptionText = new Span(description);
        descriptionText.addClassName("card--description");

        add(titleText, descriptionText);
    }
}
