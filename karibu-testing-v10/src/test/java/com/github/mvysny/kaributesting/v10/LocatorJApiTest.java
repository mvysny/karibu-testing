package com.github.mvysny.kaributesting.v10;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import static com.github.mvysny.kaributesting.v10.LocatorJ.*;
import static com.github.mvysny.kaributesting.v10.GridKt.*;

/**
 * Not really a test, doesn't run any test methods. It just fails compilation if the LocatorJ API
 * is hard to use from Java. This is an API test.
 * @author mavi
 */
@SuppressWarnings("removal")
public class LocatorJApiTest {

    public static class MainView extends VerticalLayout {}

    public static class MyUI extends UI {}
    public LocatorJApiTest() {
        MockVaadin.setup(new Routes(), MyUI::new);
        MockVaadin.setup(new Routes().autoDiscoverViews("com.vaadin.flow.demo"));
        final MainView main = (MainView) UI.getCurrent().getChildren().findFirst().get();
        main.getChildren().count();

        _get(H1.class);
        _get(H1.class, spec -> spec.withLabel("Name:").withId("foo"));
        _get(new Button(), TextField.class);
        _get(new VerticalLayout(), TextField.class, spec -> spec.withText("Name:").withId("foo").withCount(0));

        _find(H1.class);
        _find(H1.class, spec -> spec.withCaption("Name:").withId("foo"));
        _find(new Button(), TextField.class);
        _find(new VerticalLayout(), TextField.class, spec -> spec.withCaption("Name:").withId("foo"));

        _assertNone(H1.class);
        _assertNone(H1.class, spec -> spec.withCaption("Name:").withId("foo"));
        _assertNone(new Button(), TextField.class);
        _assertNone(new VerticalLayout(), TextField.class, spec -> spec.withCaption("Name:").withId("foo"));

        _assertOne(H1.class);
        _assertOne(H1.class, spec -> spec.withCaption("Name:").withId("foo"));
        _assertOne(H1.class, spec -> spec.withCaption("Name:").withId("foo").withCount(0));
        _assertOne(new Button(), TextField.class);
        _assertOne(new VerticalLayout(), TextField.class, spec -> spec.withCaption("Name:").withId("foo").withoutClasses("current"));
        _assertOne(new Icon(VaadinIcon.ABACUS), Icon.class, spec -> spec.withIcon(VaadinIcon.ABACUS));
        _assertOne(new Button(VaadinIcon.ABACUS.create()), Button.class, spec -> spec.withIcon(VaadinIcon.ABACUS));

        _assert(H1.class, 2);
        _assert(H1.class, 3, spec -> spec.withCaption("Name:").withId("foo"));
        _assert(H1.class, 4, spec -> spec.withCaption("Name:").withId("foo").withCount(0));
        _assert(new Button(), TextField.class, 6);
        _assert(new VerticalLayout(), TextField.class, 3, spec -> spec.withCaption("Name:").withId("foo"));

        _click(new Button());
        _click(new Icon());
        _setValue(new TextField(), "John");
        _fireValueChange(new TextField());

        @SuppressWarnings("unchecked")
        final Grid<Person> grid = _get(Grid.class);

        final AddNewPersonForm form = new AddNewPersonForm();
        _setValue(_get(form, TextField.class, spec -> spec.withCaption("Name:")), "John Doe");
        _click(_get(form, Button.class, spec -> spec.withCaption("Create")));

        _get(new Grid<>(), 0);
        _size(new Grid<>());
        _getFormattedRow(new Grid<>(), 5);
        expectRows(new Grid<>(), 0);
        expectRow(new Grid<>(), 0, "John Doe", "25");
        _clickItem(new Grid<>(), 0);
        _doubleClickItem(new Grid<>(), 0);
        _clickRenderer(new Grid<>(), 0, "edit");

        _assertEnabled(new Button());
        final Button disabledButton = new Button();
        disabledButton.setEnabled(false);
        _assertDisabled(disabledButton);
        _assertNotReadOnly(new TextField());
        final TextField readOnlyTF = new TextField();
        readOnlyTF.setReadOnly(true);
        _assertReadOnly(readOnlyTF);

        _fireValueChange(new TextField());
        _fireValueChange(new TextField(), true);
    }

    public static class Person {}

    public static class AddNewPersonForm extends VerticalLayout {}
}
