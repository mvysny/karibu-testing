package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.UI
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
class LocatorGTest {
    @BeforeEach void setup() {
        TestAssumptions.assumeTestable()
        MockVaadin.setup()
    }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test
    void apiTest() {
        expect(UI.current) { LocatorG._get(UI) }
        expect(UI.current) { LocatorG._get(UI) { withoutClasses = "foo" } }
        LocatorG._find(UI)
        LocatorG._find(UI) { text = "foo" }
        UI.current.add(new TextField("bar"))
        LocatorG._expectOne(TextField)
        LocatorG._expectOne(TextField) { caption = "bar"}
        LocatorG._expectNone(TextField) { caption = "baz"}
        LocatorG._expectNone(TextArea)
        LocatorG._expect(TextArea, 0)
        LocatorG._expect(TextArea, 0) { caption = "foo" }
    }
}
