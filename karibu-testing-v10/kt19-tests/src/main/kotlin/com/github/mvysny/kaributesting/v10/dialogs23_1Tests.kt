package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Span

@DynaTestDsl
fun DynaNodeGroup.dialog23_1tests() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("lookup in header+footer") {
        val dialog = Dialog()
        dialog.header.add(Span("Foo"))
        dialog.footer.add(Button("Bar"))
        dialog.open()
        _expectOne<Span> { caption = "Foo" }
        _expectOne<Button> { caption = "Bar" }
    }
}
