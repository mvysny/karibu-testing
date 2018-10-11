package com.github.karibu.testing;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import static com.github.karibu.testing.LocatorJ._get;

/**
 * Not really a test, doesn't run any test methods. It just fails compilation if the LocatorJ API
 * is hard to use from Java. This is an API test.
 * @author mavi
 */
public class LocatorJTest {
    public LocatorJTest() {
        _get(Label.class);
        _get(Label.class, spec -> spec.withCaption("Name:").withId("foo"));
        _get(new Button(), TextField.class);
        _get(new VerticalLayout(), TextField.class, spec -> spec.withCaption("Name:").withId("foo"));
    }
}
