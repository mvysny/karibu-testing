package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.textfield.TextField
import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Merely a test of the API. No need to test the implementation, it is thoroughly tested elsewhere.
 * @author mavi
 */
@CompileStatic
class HasValueUtilsTest {
    @BeforeEach void setup() {
        TestAssumptions.assumeTestable()
        MockVaadin.setup()
    }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test void api() {
        new TextField()._value
        new TextField()._value = "bar"
        new TextField()._fireValueChange()
        new Checkbox()._fireValueChange()
    }
}
