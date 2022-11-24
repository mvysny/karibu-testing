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
import kotlin.test.expect

@Suppress("DEPRECATION")
@DynaTestDsl
internal fun DynaNodeGroup.locatorAddonsTests() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("captionContains") {
        test("fails when caption doesn't match") {
            UI.getCurrent().button("bar")
            expectThrows<AssertionError>("and captionContains('foo')") {
                _get<Button> { captionContains("foo") }
            }
        }
        test("succeeds when caption matches") {
            UI.getCurrent().add(Button("foo bar"))
            _get<Button> { captionContains("foo") }
        }
        test("picks proper button when caption matches") {
            UI.getCurrent().apply {
                verticalLayout {
                    button("foo bar")
                    button("baz")
                }
            }
            expect("foo bar") { _get<Button> { captionContains("foo") }.caption }
        }
    }

    group("labelContains") {
        test("fails when caption doesn't match") {
            UI.getCurrent().button("foo")
            expectThrows<AssertionError>("and labelContains('foo')") {
                _get<Button> { labelContains("foo") }
            }
        }
        test("succeeds when caption matches") {
            UI.getCurrent().add(TextField("foo bar"))
            _get<TextField> { labelContains("foo") }
        }
        test("picks proper component when caption matches") {
            UI.getCurrent().apply {
                verticalLayout {
                    textField("foo bar")
                    textField("baz")
                }
            }
            expect("foo bar") { _get<TextField> { labelContains("foo") }.caption }
        }
    }

    group("textContains") {
        test("fails when text doesn't match") {
            UI.getCurrent().span("bar")
            expectThrows<AssertionError>("and textContains('foo')") {
                _get<Span> { textContains("foo") }
            }
        }
        test("succeeds when text matches") {
            UI.getCurrent().span("foo bar")
            _get<Span> { textContains("foo") }
        }
        test("picks proper span when text matches") {
            UI.getCurrent().apply {
                verticalLayout {
                    span("foo bar")
                    span("baz")
                }
            }
            expect("foo bar") { _get<Span> { textContains("foo") }.text }
        }
    }
    group("iconIs") {
        test("smoke") {
            UI.getCurrent().button("iconless")
            val btn: Button = UI.getCurrent().iconButton(VaadinIcon.VAADIN_H.create())
            UI.getCurrent().iconButton(VaadinIcon.HOURGLASS.create())
            val ic = UI.getCurrent().icon(VaadinIcon.ABACUS)
            expect(btn) { _get<Button> { iconIs(VaadinIcon.VAADIN_H) } }
            expect(ic) { _get<Icon> { iconIs(VaadinIcon.ABACUS) } }
        }
    }
}
