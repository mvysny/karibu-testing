package com.github.karibu.testing.v10

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.vok.karibudsl.flow.button
import com.github.vok.karibudsl.flow.verticalLayout
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
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
        test("picks proper label when caption matches") {
            UI.getCurrent().apply {
                verticalLayout {
                    button("foo bar")
                    button("baz")
                }
            }
            expect("foo bar") { _get<Button> { captionContains("foo") }.caption }
        }
    }
})
