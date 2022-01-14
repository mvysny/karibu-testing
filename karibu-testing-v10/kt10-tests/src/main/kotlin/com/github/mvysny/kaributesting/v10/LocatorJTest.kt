package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import kotlin.test.expect

/**
 * A very simple quick test of the [LocatorJ] class.
 */
@DynaTestDsl
internal fun DynaNodeGroup.locatorJTest() {

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
            val button = Button().apply { id_ = "foo" }
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


    group("search spec") {
        fun Component.matches(spec: SearchSpecJ<Component>.()->Unit): Boolean = SearchSpecJ(SearchSpec(Component::class.java)).apply { spec() }.toPredicate().test(this)
        test("id") {
            expect(true) { Button().matches { } }
            expect(false) { Button().matches { withId("a") } }
            expect(true) { Button().apply { id_ = "a" } .matches { withId("a") } }
            expect(true) { Button().apply { id_ = "a" } .matches { } }
            expect(false) { Button().apply { id_ = "a b" } .matches { withId("a") } }
            expect(false) { Button().apply { id_ = "a" } .matches { withId("a b") } }
        }
        test("caption") {
            expect(true) { Button("click me").matches { withCaption("click me") } }
            expect(true) { TextField("name:").matches { withCaption("name:") } }
            expect(true) { Button("click me").matches { } }
            expect(true) { TextField("name:").matches { } }
            expect(false) { Button("click me").matches { withCaption("Click Me") } }
            expect(false) { TextField("name:").matches { withCaption("Name") } }
        }
        test("placeholder") {
            expect(true) { TextField("name").apply { placeholder = "the name" } .matches { withPlaceholder("the name") } }
            expect(true) { PasswordField("password").apply { placeholder = "at least 6 characters" }.matches { withPlaceholder("at least 6 characters") } }
            expect(true) { ComboBox<String>().apply { placeholder = "foo" }.matches { withPlaceholder("foo") } }
            expect(false) { TextField("name").apply { placeholder = "the name" } .matches { withPlaceholder("name") } }
            expect(false) { PasswordField("password").apply { placeholder = "at least 6 characters" }.matches { withPlaceholder("password") } }
        }
        test("value") {
            expect(true) { TextField(null, "Mannerheim", "placeholder").matches { withValue("Mannerheim") } }
            expect(false) { TextField("Mannerheim").matches { withValue("Mannerheim") } }
        }
        test("styles") {
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { } }
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { withClasses("a") } }
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { withClasses("b") } }
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { withClasses("a b") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withClasses("a c") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withClasses("c") } }
        }
        test("withoutStyles") {
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("a") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("b") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("a b") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("a c") } }
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("c") } }
        }
        test("predicates") {
            expect(true) { Button().matches {}}
            expect(false) { Button().matches { withPredicate { false } }}
        }
    }

    test("_assertNoDialogs() smoke") {
        LocatorJ._assertNoDialogs()
    }
}
