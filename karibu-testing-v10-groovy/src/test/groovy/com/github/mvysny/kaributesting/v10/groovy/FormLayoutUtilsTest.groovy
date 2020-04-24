package com.github.mvysny.kaributesting.v10.groovy

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.textfield.TextField
import groovy.transform.CompileStatic
import kotlin.test.AssertionsKt
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Merely a test of the API. No need to test the implementation, it is thoroughly tested elsewhere.
 * @author mavi
 */
@CompileStatic
class FormLayoutUtilsTest {
    @BeforeEach void setup() {
        TestAssumptions.assumeTestable()
        MockVaadin.setup()
    }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test
    void apiTest() {
        def tf = new TextField()
        FormLayout.FormItem item = new FormLayout().addFormItem(tf, "foo")
        AssertionsKt.expect("foo") { item.caption }
        AssertionsKt.expect(tf) { item.field }
    }
}
