package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class AbstractDialogTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    // for other Dialog-related tests see MockVaadinTest.kt

    @Test fun `no dialog`() {
        _expectNone<Dialog>()
    }

    @Test fun `simple dialog`() {
        // tests for https://github.com/mvysny/karibu-testing/issues/166
        val dlg = Dialog()
        dlg.open()
        _expectOne<Dialog>()
        dlg.close()
        _expectNone<Dialog>()
    }

    // tests https://github.com/mvysny/karibu-testing/issues/102
    @Test fun `nested modal dialogs`() {
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

    // for other Composite-related tests see CompositeTest.kt
    @Test fun `CompositeDialog causes footer components to appear two times in Karibu tree dump`() {
        class MyDialog : Composite<Dialog>() {
            init {
                content.footer.add(Button("Hi!"))
            }
        }

        val dlg = MyDialog()
        dlg._expectOne<Button> { text = "Hi!" }
    }
}
