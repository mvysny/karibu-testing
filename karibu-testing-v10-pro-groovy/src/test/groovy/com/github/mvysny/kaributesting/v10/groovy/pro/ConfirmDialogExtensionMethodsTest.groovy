package com.github.mvysny.kaributesting.v10.groovy.pro

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Merely a test of the API. No need to test the implementation, it is thoroughly tested elsewhere.
 * @author mavi
 */
@CompileStatic
class ConfirmDialogExtensionMethodsTest {
    @BeforeEach void setup() {
        MockVaadin.setup()
    }
    @AfterEach void teardown() { MockVaadin.tearDown() }

    @Test
    void apiTest() {
        new ConfirmDialog()._fireReject()
        new ConfirmDialog()._fireCancel()
        new ConfirmDialog()._fireConfirm()
        new ConfirmDialog().getText()
        new ConfirmDialog().getTextComponents()
        new ConfirmDialog().getHeader()
        new ConfirmDialog().getHeaderComponents()
    }
}
