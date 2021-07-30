package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.karibudsl.v10.button
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span

internal fun DynaNodeGroup.elementUtilsTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("getVirtualChildren()") {
        test("initially empty") {
            expectList() { Div().element.getVirtualChildren() }
            expectList() { Span().element.getVirtualChildren() }
            expectList() { UI.getCurrent().button().element.getVirtualChildren() }
        }
        test("add virtual child") {
            val span = Span().element
            val parent = Div()
            parent.element.appendVirtualChild(span)
            expectList(span) { parent.element.getVirtualChildren() }
        }
    }
}
