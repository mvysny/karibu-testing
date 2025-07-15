package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.card.Card
import com.vaadin.flow.component.html.Span
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class AbstractCardTests {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun smoke() {
        UI.getCurrent().add(Card())
    }

    @Test fun `child component lookup`() {
        val card = Card()
        card.title = Span("Lapland")
        card.subtitle = Span("The Exotic North")
        card.add(Span("Card text contents"))
        card.headerPrefix = Button("prefix")
        card.headerSuffix = Button("suffix")
        card.addToFooter(Button("Book Vacation"))

        card._expectOne<Span> { text = "Lapland" }
        card._expectOne<Span> { text = "The Exotic North" }
        card._expectOne<Span> { text = "Card text contents" }
        card._expectOne<Button> { text = "prefix" }
        card._expectOne<Button> { text = "suffix" }
        card._expectOne<Button> { text = "Book Vacation" }
    }
}