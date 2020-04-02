package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.select.Select
import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Merely a test of the API. No need to test the implementation, it is thoroughly tested elsewhere.
 * @author mavi
 */
@CompileStatic
class ComboBoxUtilsTest {
    @BeforeEach void setup() {
        TestAssumptions.assumeTestable()
        MockVaadin.setup()
    }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test
    void apiTest() {
        new ComboBox().setUserInput("foo")
        def combobox = new ComboBox()
        combobox.setAllowCustomValue(true)
        combobox._fireCustomValueSet("foo")
        def comboWithStrings = new ComboBox<String>()
        comboWithStrings.setItems(["a", "b", "c"])
        List<String> items = comboWithStrings.getSuggestionItems()
        def comboWithObjects = new ComboBox<Object>()
        comboWithObjects.setItems([new Object()])
        items = comboWithObjects.getSuggestions()
        def selectWithStrings = new Select<String>()
        selectWithStrings.setItems([])
        items = selectWithStrings.getSuggestionItems()
        def selectWithObjects = new Select<Object>()
        selectWithObjects.setItems([])
        items = selectWithObjects.getSuggestions()
    }
}
