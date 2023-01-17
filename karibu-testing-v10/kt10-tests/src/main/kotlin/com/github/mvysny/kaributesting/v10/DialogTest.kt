package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.dialog.Dialog

@DynaTestDsl
internal fun DynaNodeGroup.dialogTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    // for other Dialog-related tests see MockVaadinTest.kt
    group("dialogs") {

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
}
