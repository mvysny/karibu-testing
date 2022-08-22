package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.dom.DomEvent
import elemental.json.Json
import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

/**
 * Merely a test of the API. No need to test the implementation, it is thoroughly tested elsewhere.
 * @author mavi
 */
@CompileStatic
class BasicUtilsTest {
    @BeforeEach void setup() {
        MockVaadin.setup()
    }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test void api() {
        new Button()._fireEvent(new ClickEvent<Component>(new Button()))
        new Button().element._fireDomEvent(new DomEvent(new Button().element, "foo", Json.createObject()))
        new Button()._fireDomEvent("foo")
        new Checkbox().getLabel()
        new Checkbox().setLabel("foo")
        new Checkbox().getCaption()
        new Checkbox().setCaption("foo")
        new TextField().id_
        UI.current.isAttached()
        UI.current._isVisible
        new Button()._text
        new Button().checkEditableByUser()
        def button = new Button()
        button.setEnabled(false)
        button.expectNotEditableByUser()
        button._expectDisabled()
        new Checkbox().element.getTextRecursively2()
        new Checkbox().element.getTextRecursively()
        new Checkbox().isEnabled()
        new TextField().placeholder
        new TextField().placeholder = ""
        new Button().removeFromParent()
        assertTrue(new Button("Hello!").matches { caption = "Hello!" })
        assertFalse(new Button("Hello!").matches { caption = "Bar" })
        new TextArea()._focus()
        new TextArea()._blur()
        Notification.show("foo").text
        new Icon().iconName
        new Icon().iconName = null
        new TextField()._getVirtualChildren()
        new Button()._expectEnabled()
    }

    @Test void api2() {
        UI.current._close()
    }
}
