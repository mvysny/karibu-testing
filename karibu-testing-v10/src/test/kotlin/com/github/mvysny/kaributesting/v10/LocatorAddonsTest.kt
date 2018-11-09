package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.span
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Span
import kotlin.test.expect

class LocatorAddonsTest : DynaTest({

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("captionContains") {
        test("fails when caption doesn't match") {
            UI.getCurrent().add(Button("bar"))
            expectThrows(IllegalArgumentException::class, message = "and captionContains('foo')") {
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

    group("textContains") {
        test("fails when text doesn't match") {
            UI.getCurrent().add(Span("bar"))
            expectThrows(IllegalArgumentException::class, message = "and textContains('foo')") {
                _get<Span> { textContains("foo") }
            }
        }
        test("succeeds when text matches") {
            UI.getCurrent().add(Span("foo bar"))
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
})
