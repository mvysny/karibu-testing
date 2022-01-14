package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.componentfactory.EnhancedDialog
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.textfield.TextField

@DynaTestDsl
internal fun DynaNodeGroup.dialogTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

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
