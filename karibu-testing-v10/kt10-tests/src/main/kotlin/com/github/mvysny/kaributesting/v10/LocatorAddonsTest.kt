package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.caption
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.textfield.TextField
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

@Suppress("DEPRECATION")
abstract class AbstractLocatorAddonsTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class captionContains {
        @Test fun `fails when caption doesn't match`() {
            UI.getCurrent().button("bar")
            expectThrows<AssertionError>("and captionContains('foo')") {
                _get<Button> { captionContains("foo") }
            }
        }
        @Test fun `succeeds when caption matches`() {
            UI.getCurrent().add(Button("foo bar"))
            _get<Button> { captionContains("foo") }
        }
        @Test fun `picks proper button when caption matches`() {
            UI.getCurrent().apply {
                verticalLayout {
                    button("foo bar")
                    button("baz")
                }
            }
            expect("foo bar") { _get<Button> { captionContains("foo") }.caption }
        }
    }

    @Nested inner class labelContains() {
        @Test fun `fails when caption doesn't match`() {
            UI.getCurrent().button("foo")
            expectThrows<AssertionError>("and labelContains('foo')") {
                _get<Button> { labelContains("foo") }
            }
        }
        @Test fun `succeeds when caption matches`() {
            UI.getCurrent().add(TextField("foo bar"))
            _get<TextField> { labelContains("foo") }
        }
        @Test fun `picks proper component when caption matches`() {
            UI.getCurrent().apply {
                verticalLayout {
                    textField("foo bar")
                    textField("baz")
                }
            }
            expect("foo bar") { _get<TextField> { labelContains("foo") }.caption }
        }
    }

    @Nested inner class textContains {
        @Test fun `fails when text doesn't match`() {
            UI.getCurrent().span("bar")
            expectThrows<AssertionError>("and textContains('foo')") {
                _get<Span> { textContains("foo") }
            }
        }
        @Test fun `succeeds when text matches`() {
            UI.getCurrent().span("foo bar")
            _get<Span> { textContains("foo") }
        }
        @Test fun `picks proper span when text matches`() {
            UI.getCurrent().apply {
                verticalLayout {
                    span("foo bar")
                    span("baz")
                }
            }
            expect("foo bar") { _get<Span> { textContains("foo") }.text }
        }
    }
    @Nested inner class iconIs {
        @Test fun smoke() {
            UI.getCurrent().button("iconless")
            val btn: Button = UI.getCurrent().iconButton(VaadinIcon.VAADIN_H.create())
            UI.getCurrent().iconButton(VaadinIcon.HOURGLASS.create())
            val ic = UI.getCurrent().icon(VaadinIcon.ABACUS)
            expect(btn) { _get<Button> { iconIs(VaadinIcon.VAADIN_H) } }
            expect(ic) { _get<Icon> { iconIs(VaadinIcon.ABACUS) } }
        }
    }
}
