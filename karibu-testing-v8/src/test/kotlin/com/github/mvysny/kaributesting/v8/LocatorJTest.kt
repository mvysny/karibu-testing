package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v8.verticalLayout
import com.vaadin.ui.Button
import com.vaadin.ui.Label
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import kotlin.test.expect

/**
 * A very simple quick test of the [LocatorJ] class.
 */
class LocatorJTest : DynaTest({

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("_get") {
        test("FailsOnNoComponents UI") {
            expectThrows(AssertionError::class) {
                LocatorJ._get(Label::class.java)
            }
        }

        test("FailsOnNoComponents") {
            expectThrows(AssertionError::class) {
                LocatorJ._get(Button(), Label::class.java)
            }
        }

        test("fails when multiple components match") {
            expectThrows(AssertionError::class) {
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
            val button = Button().apply { id = "foo" }
            expect(listOf(button)) { LocatorJ._find(VerticalLayout(button, Button()), Button::class.java) { it.withId("foo") } }
        }
    }

    group("_expectNone") {
        test("succeeds on no matched components") {
            LocatorJ._assertNone(Button(), Label::class.java)
        }

        test("fails when multiple components match") {
            expectThrows(AssertionError::class) {
                LocatorJ._assertNone(UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }, VerticalLayout::class.java)
            }
        }

        test("selects self") {
            expectThrows(AssertionError::class) { LocatorJ._assertNone(Button(), Button::class.java) }
        }

        test("ReturnsNested") {
            expectThrows(AssertionError::class) { LocatorJ._assertNone(VerticalLayout(Button()), Button::class.java) }
        }
    }

    group("_expectOne") {
        test("FailsOnNoComponents UI") {
            expectThrows(AssertionError::class) {
                LocatorJ._assertOne(Label::class.java)
            }
        }

        test("FailsOnNoComponents") {
            expectThrows(AssertionError::class) {
                LocatorJ._assertOne(Button(), Label::class.java)
            }
        }

        test("fails when multiple components match") {
            expectThrows(AssertionError::class) {
                LocatorJ._assertOne(UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }, VerticalLayout::class.java)
            }
        }

        test("selects self") {
            LocatorJ._assertOne(Button(), Button::class.java)
        }

        test("ReturnsNested") {
            LocatorJ._assertOne(VerticalLayout(Button()), Button::class.java)
        }
    }

    group("_expect") {
        test("FailsOnNoComponents UI") {
            expectThrows(AssertionError::class) { LocatorJ._assert(Label::class.java, 1) }
        }

        test("FailsOnNoComponents") {
            expectThrows(AssertionError::class) { LocatorJ._assert(Button(), Label::class.java, 1) }
        }

        test("matching 0 components works") {
            LocatorJ._assert(Label::class.java, 0)
            LocatorJ._assert(Button(), Label::class.java, 0)
        }

        test("fails when the count is wrong") {
            expectThrows(AssertionError::class) {
                LocatorJ._assert(UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }, VerticalLayout::class.java, 1)
            }
        }

        test("succeeds when the count is right") {
            LocatorJ._assert(UI.getCurrent().verticalLayout {
                verticalLayout { }
            }, VerticalLayout::class.java, 2)
        }

        test("selects self") {
            LocatorJ._assert(Button(), Button::class.java, 1)
        }

        test("spec") {
            expectThrows(AssertionError::class) {
                LocatorJ._assert(Button("foo"), Button::class.java, 1) { it.withCaption("bar") }
            }
            expectThrows(AssertionError::class) {
                LocatorJ._assert(Button("foo"), Button::class.java, 1) { it.withCaption("bar") }
            }
        }
    }
})
