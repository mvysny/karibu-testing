package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.karibudsl.v10.text
import com.vaadin.componentfactory.EnhancedDialog
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.details.Details
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.textfield.TextField
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.detailsTests() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    // for other Dialog-related tests see MockVaadinTest.kt
    test("smoke") {
        val d = Details(Span("Contact"), Span("Foo"))
        d._expectOne<Span> { text = "Contact" }
        d._expectOne<Span> { text = "Foo" }

        d.isOpened = true
        d._expectOne<Span> { text = "Contact" }
        d._expectOne<Span> { text = "Foo" }

        d.isOpened = false
        d._expectOne<Span> { text = "Contact" }
        d._expectOne<Span> { text = "Foo" }
    }
}
