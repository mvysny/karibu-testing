package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog

@DynaTestDsl
internal fun DynaNodeGroup.compositeTests() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

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
