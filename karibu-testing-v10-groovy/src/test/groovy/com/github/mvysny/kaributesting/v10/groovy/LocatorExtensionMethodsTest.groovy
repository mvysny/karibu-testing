package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static kotlin.test.AssertionsKt.expect

/**
 * @author mavi
 */
@CompileStatic
class LocatorExtensionMethodsTest {
    @BeforeEach void setup() { MockVaadin.setup() }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test
    void apiTest() {
        expect(UI.current) { UI.current._get(UI) }
        expect(UI.current) { UI.current._get(UI) { withoutClasses = "foo" } }
        UI.current._find(UI)
        UI.current._find(UI) { text = "foo" }
        UI.current.add(new TextField("bar"))
        UI.current._expectOne(TextField)
        UI.current._expectOne(TextField) { caption = "bar"}
        UI.current._expectNone(TextField) { caption = "baz"}
        UI.current._expectNone(TextArea)
        UI.current._expect(TextArea, 0)
        UI.current._expect(TextArea, 0) { caption = "foo" }
        new Button()._expect(Button)
    }
}
