package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.details.Details
import com.vaadin.flow.component.html.Span
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class AbstractDetailsTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    // for other Dialog-related tests see MockVaadinTest.kt
    @Test fun smoke() {
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
