package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog

@DynaTestDsl
internal fun DynaNodeGroup.dialogTests() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    // for other Dialog-related tests see MockVaadinTest.kt

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

    // for other Composite-related tests see CompositeTest.kt
    test("Composite<Dialog> causes footer components to appear two times in Karibu tree dump") {
        class MyDialog : Composite<Dialog>() {
            init {
                content.footer.add(Button("Hi!"))
            }
        }

        val dlg = MyDialog()
        dlg._expectOne<Button> { text = "Hi!" }
    }
}
