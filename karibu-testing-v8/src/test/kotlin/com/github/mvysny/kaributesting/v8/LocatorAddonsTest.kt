package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v8.button
import com.github.mvysny.karibudsl.v8.verticalLayout
import com.vaadin.ui.Button
import com.vaadin.ui.UI
import kotlin.test.expect

class LocatorAddonsTest : DynaTest({

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("captionContains") {
        test("fails when caption doesn't match") {
            UI.getCurrent().content = Button("bar")
            expectThrows(IllegalArgumentException::class, message = "and captionContains('foo')") {
                _get<Button> { captionContains("foo") }
            }
        }
        test("succeeds when caption matches") {
            UI.getCurrent().content = Button("foo bar")
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
