package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.v10.pro._fireCancel
import com.github.mvysny.kaributesting.v10.pro._fireConfirm
import com.github.mvysny.kaributesting.v10.pro._fireReject
import com.github.mvysny.kaributesting.v10.pro._getHeader
import com.github.mvysny.kaributesting.v10.pro._getText
import com.github.mvysny.kaributesting.v10.pro.getHeader
import com.github.mvysny.kaributesting.v10.pro.getHeaderComponents
import com.github.mvysny.kaributesting.v10.pro.getText
import com.github.mvysny.kaributesting.v10.pro.getTextComponents
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.expect
import kotlin.test.fail

abstract class AbstractConfirmDialogTests {
    @BeforeEach
    fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach
    fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test
    fun `smoke test`() {
        ConfirmDialog("Foo", "Bar", "Yes") {}.open()
        _expectOne<ConfirmDialog>()
    }

    @Test
    fun confirm() {
        var confirmCalled = false
        ConfirmDialog("Foo", "Bar", "Yes") { confirmCalled = true }.open()
        _get<ConfirmDialog>()._fireConfirm()
        expect(true) { confirmCalled }
        _expectNone<ConfirmDialog>()  // make sure the ConfirmDialog is closed: https://github.com/mvysny/karibu-testing/issues/34
    }

    @Test
    fun cancel() {
        var cancelCalled = false
        val dialog = ConfirmDialog("Foo", "Bar", "Yes") { fail("unexpected") }
        dialog.addCancelListener { cancelCalled = true }
        dialog.open()
        _get<ConfirmDialog>()._fireCancel()
        expect(true) { cancelCalled }
        _expectNone<ConfirmDialog>()  // make sure the ConfirmDialog is closed: https://github.com/mvysny/karibu-testing/issues/34
    }

    @Test
    fun reject() {
        var rejectCalled = false
        val dialog = ConfirmDialog("Foo", "Bar", "Yes") { fail("unexpected") }
        dialog.addCancelListener { fail("unexpected") }
        dialog.addRejectListener { rejectCalled = true }
        dialog.open()
        _get<ConfirmDialog>()._fireReject()
        expect(true) { rejectCalled }
        _expectNone<ConfirmDialog>()  // make sure the ConfirmDialog is closed: https://github.com/mvysny/karibu-testing/issues/34
    }

    @Test
    fun getText() {
        expect(null) { ConfirmDialog().getText() }
        expect("Bar") { ConfirmDialog("Foo", "Bar", "Yes") {}.getText() }
    }

    @Test
    fun getTextComponents() {
        expectList() { ConfirmDialog().getTextComponents() }
        val dlg = ConfirmDialog()
        val textSpan = Span("Foo")
        dlg.setText(textSpan)
        expectList(textSpan) { dlg.getTextComponents() }
    }

    @Test
    fun getHeader() {
        expect(null) { ConfirmDialog().getHeader() }
        expect("Foo") { ConfirmDialog("Foo", "Bar", "Yes") {}.getHeader() }
    }

    @Test
    fun getHeaderComponents() {
        expectList() { ConfirmDialog().getHeaderComponents() }
        val dlg = ConfirmDialog()
        val textSpan = Span("Foo")
        dlg.setHeader(textSpan)
        expectList(textSpan) { dlg.getHeaderComponents() }
    }

    @Test
    fun _getHeader() {
        expect("") { ConfirmDialog()._getHeader() }
        expect("Foo") { ConfirmDialog("Foo", "Bar", "Yes") {}._getHeader() }
        val dlg = ConfirmDialog()
        dlg.setHeader(H2("Are you sure?"))
        expect("Are you sure?") { dlg._getHeader() }
    }

    @Test
    fun _getText() {
        expect("") { ConfirmDialog()._getText() }
        expect("Bar") { ConfirmDialog("Foo", "Bar", "Yes") {}._getText() }
        val dlg = ConfirmDialog()
        dlg.setText(H2("Important message"))
        expect("Important message") { dlg._getText() }
    }
}