package com.github.karibu.testing;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import static com.github.karibu.testing.LocatorJ.*;

/**
 * Not really a test, doesn't run any test methods. It just fails compilation if the LocatorJ API
 * is hard to use from Java. This is an API test.
 * @author mavi
 */
public class LocatorJTest {
    public static class MyUI extends UI {
        @Override
        protected void init(VaadinRequest request) {
        }
    }
    public LocatorJTest() {
        MockVaadin.setup(MyUI::new);

        _get(Label.class);
        _get(Label.class, spec -> spec.withCaption("Name:").withId("foo"));
        _get(new Button(), TextField.class);
        _get(new VerticalLayout(), TextField.class, spec -> spec.withCaption("Name:").withId("foo"));

        _find(Label.class);
        _find(Label.class, spec -> spec.withCaption("Name:").withId("foo"));
        _find(new Button(), TextField.class);
        _find(new VerticalLayout(), TextField.class, spec -> spec.withCaption("Name:").withId("foo"));

        _assertNone(Label.class);
        _assertNone(Label.class, spec -> spec.withCaption("Name:").withId("foo"));
        _assertNone(new Button(), TextField.class);
        _assertNone(new VerticalLayout(), TextField.class, spec -> spec.withCaption("Name:").withId("foo"));

        _click(new Button());
        _setValue(new TextField(), "John");
    }
}
