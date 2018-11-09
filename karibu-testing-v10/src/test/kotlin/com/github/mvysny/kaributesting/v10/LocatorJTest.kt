package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import kotlin.test.expect

/**
 * A very simple quick test of the [LocatorJ] class.
 */
class LocatorJTest : DynaTest({

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("_get") {
        test("FailsOnNoComponents UI") {
            expectThrows(IllegalArgumentException::class) {
                LocatorJ._get(Label::class.java)
            }
        }

        test("FailsOnNoComponents") {
            expectThrows(IllegalArgumentException::class) {
                LocatorJ._get(Button(), Label::class.java)
            }
        }

        test("fails when multiple components match") {
            expectThrows(IllegalArgumentException::class) {
                LocatorJ._get(UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }, VerticalLayout::class.java)
            }
        }

        test("selects self") {
            val button = Button("foo")
            expect(button) { LocatorJ._get(button, Button::class.java) }
            expect(button) { LocatorJ._get(button, Button::class.java) { it.withCaption("foo") } }
        }

        test("ReturnsNested") {
            val button = Button()
            expect(button) { LocatorJ._get(VerticalLayout(button), Button::class.java) }
        }
    }

    group("_find") {
        test("findMatchingId") {
            val button = Button().apply { id_ = "foo" }
            expect(listOf(button)) { LocatorJ._find(VerticalLayout(button, Button()), Button::class.java) { it.withId("foo") } }
        }
    }

    group("_expectNone") {
        test("succeeds on no matched components") {
            LocatorJ._assertNone(Button(), Label::class.java)
        }

        test("fails when multiple components match") {
            expectThrows(IllegalArgumentException::class) {
                LocatorJ._assertNone(UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }, VerticalLayout::class.java)
            }
        }

        test("selects self") {
            expectThrows(IllegalArgumentException::class) { LocatorJ._assertNone(Button(), Button::class.java) }
        }

        test("ReturnsNested") {
            expectThrows(IllegalArgumentException::class) { LocatorJ._assertNone(VerticalLayout(Button()), Button::class.java) }
        }
    }
})
