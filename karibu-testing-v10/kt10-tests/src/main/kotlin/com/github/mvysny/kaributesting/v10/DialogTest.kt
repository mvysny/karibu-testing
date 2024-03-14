package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.karibudsl.v10.text
import com.vaadin.componentfactory.EnhancedDialog
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.textfield.TextField
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.dialogTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    // for other Dialog-related tests see MockVaadinTest.kt
    group("dialogs") {

        test("no dialog") {
            _expectNone<Dialog>()
        }

        test("simple dialog") {
            // tests for https://github.com/mvysny/karibu-testing/issues/166
            val dlg = Dialog()
            dlg.open()
            _expectOne<Dialog>()
            dlg.close()
            _expectNone<Dialog>()
        }

        // tests https://github.com/mvysny/karibu-testing/issues/102
        test("nested modal dialogs") {
            val dialog = Dialog()
            dialog.isModal = true
            dialog.open()
            _expectOne<Dialog>()

            val nestedDialog = Dialog()
            nestedDialog.isModal = true
            nestedDialog.open()
            _expect<Dialog>(2)

            nestedDialog.close()
            _expectOne<Dialog>()

            dialog.close()
            _expectNone<Dialog>()
        }
    }

    group("enhanced dialog") {

        test("enhanced dialog components are discovered") {
            val title = Span("A span added to the enhanced dialog header")
            val field = TextField()
            val close = Button("Close")
            val enhancedDialog = EnhancedDialog().apply {
                add(field)
                addToHeader(title)
                addToFooter(close)
            }

            _expectNone<EnhancedDialog>()
            enhancedDialog.open()
            _expectOne<EnhancedDialog>()

            enhancedDialog._expectOne<Span> { text = title.text }
            enhancedDialog._expectOne<TextField>()
            enhancedDialog._expectOne<Button> { text = close.text }
        }
    }
}
