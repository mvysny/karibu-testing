package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.vaadin.flow.component.details.Details
import com.vaadin.flow.component.html.Span

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
