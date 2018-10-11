package com.github.karibu.testing.v10;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import static com.github.karibu.testing.v10.LocatorJ.*;

/**
 * Not really a test, doesn't run any test methods. It just fails compilation if the LocatorJ API
 * is hard to use from Java. This is an API test.
 * @author mavi
 */
public class LocatorJTest {
    public static class MyUI extends UI {}
    public LocatorJTest() {
        MockVaadin.setup(new Routes(), MyUI::new);

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

        final Grid<Person> grid = _get(Grid.class);

        final AddNewPersonForm form = new AddNewPersonForm();
        _setValue(_get(form, TextField.class, spec -> spec.withCaption("Name:")), "John Doe");
        _click(_get(form, Button.class, spec -> spec.withCaption("Create")));

        GridKt._get(new Grid<>(), 0);
        GridKt._size(new Grid<>());
        GridKt._getFormattedRow(new Grid<>(), 5);
        GridKt.expectRows(new Grid<>(), 0);
        GridKt.expectRow(new Grid<>(), 0, "John Doe", "25");
    }

    public static class Person {}

    public static class AddNewPersonForm extends VerticalLayout {}
}
