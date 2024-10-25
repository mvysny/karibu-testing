package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

/**
 * A very simple quick test of the [LocatorJ] class.
 */
abstract class AbstractLocatorJTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class _get {
        @Test fun `FailsOnNoComponents UI`() {
            expectThrows(AssertionError::class) {
                LocatorJ._get(TextField::class.java)
            }
        }

        @Test fun FailsOnNoComponents() {
            expectThrows(AssertionError::class) {
                LocatorJ._get(Button(), TextField::class.java)
            }
        }

        @Test fun `fails when multiple components match`() {
            expectThrows(AssertionError::class) {
                LocatorJ._get(UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }, VerticalLayout::class.java)
            }
        }

        @Test fun `selects self`() {
            val button = Button("foo")
            expect(button) { LocatorJ._get(button, Button::class.java) }
            expect(button) { LocatorJ._get(button, Button::class.java) { it.withText("foo") } }
        }

        @Test fun ReturnsNested() {
            val button = Button()
            expect(button) { LocatorJ._get(VerticalLayout(button), Button::class.java) }
        }
    }

    @Nested inner class _find {
        @Test fun findMatchingId() {
            val button = Button().apply { id_ = "foo" }
            expect(listOf(button)) { LocatorJ._find(VerticalLayout(button, Button()), Button::class.java) { it.withId("foo") } }
        }
    }

    @Nested inner class _expectNone {
        @Test fun `succeeds on no matched components`() {
            LocatorJ._assertNone(Button(), TextField::class.java)
        }

        @Test fun `fails when multiple components match`() {
            expectThrows(AssertionError::class) {
                LocatorJ._assertNone(UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }, VerticalLayout::class.java)
            }
        }

        @Test fun `selects self`() {
            expectThrows(AssertionError::class) { LocatorJ._assertNone(Button(), Button::class.java) }
        }

        @Test fun ReturnsNested() {
            expectThrows(AssertionError::class) { LocatorJ._assertNone(VerticalLayout(Button()), Button::class.java) }
        }
    }

    @Nested inner class _expectOne {
        @Test fun `FailsOnNoComponents UI`() {
            expectThrows(AssertionError::class) {
                LocatorJ._assertOne(TextField::class.java)
            }
        }

        @Test fun FailsOnNoComponents() {
            expectThrows(AssertionError::class) {
                LocatorJ._assertOne(Button(), TextField::class.java)
            }
        }

        @Test fun `fails when multiple components match`() {
            expectThrows(AssertionError::class) {
                LocatorJ._assertOne(UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }, VerticalLayout::class.java)
            }
        }

        @Test fun `selects self`() {
            LocatorJ._assertOne(Button(), Button::class.java)
        }

        @Test fun ReturnsNested() {
            LocatorJ._assertOne(VerticalLayout(Button()), Button::class.java)
        }
    }

    @Nested inner class _expect {
        @Test fun `FailsOnNoComponents UI`() {
            expectThrows(AssertionError::class) { LocatorJ._assert(Button::class.java, 1) }
        }

        @Test fun FailsOnNoComponents() {
            expectThrows(AssertionError::class) { LocatorJ._assert(Button(), TextField::class.java, 1) }
        }

        @Test fun `matching 0 components works`() {
            LocatorJ._assert(NativeLabel::class.java, 0)
            LocatorJ._assert(Button(), NativeLabel::class.java, 0)
        }

        @Test fun `fails when the count is wrong`() {
            expectThrows(AssertionError::class) {
                LocatorJ._assert(UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }, VerticalLayout::class.java, 1)
            }
        }

        @Test fun `succeeds when the count is right`() {
            LocatorJ._assert(UI.getCurrent().verticalLayout {
                verticalLayout { }
            }, VerticalLayout::class.java, 2)
        }

        @Test fun `selects self`() {
            LocatorJ._assert(Button(), Button::class.java, 1)
        }

        @Test fun spec() {
            expectThrows(AssertionError::class) {
                LocatorJ._assert(Button("foo"), Button::class.java, 1) { it.withText("bar") }
            }
            expectThrows(AssertionError::class) {
                LocatorJ._assert(Button("foo"), Button::class.java, 1) { it.withText("bar") }
            }
        }
    }


    @Nested inner class `search spec` {
        fun Component.matches(spec: SearchSpecJ<Component>.()->Unit): Boolean = SearchSpecJ(SearchSpec(Component::class.java)).apply { spec() }.toPredicate().test(this)
        @Test fun id() {
            expect(true) { Button().matches { } }
            expect(false) { Button().matches { withId("a") } }
            expect(true) { Button().apply { id_ = "a" } .matches { withId("a") } }
            expect(true) { Button().apply { id_ = "a" } .matches { } }
            expect(false) { Button().apply { id_ = "a b" } .matches { withId("a") } }
            expect(false) { Button().apply { id_ = "a" } .matches { withId("a b") } }
        }
        @Test fun caption() {
            expect(true) { Button("click me").matches { withText("click me") } }
            expect(true) { TextField("name:").matches { withLabel("name:") } }
            expect(true) { Button("click me").matches { } }
            expect(true) { TextField("name:").matches { } }
            expect(false) { Button("click me").matches { withText("Click Me") } }
            expect(false) { TextField("name:").matches { withLabel("Name") } }
        }
        @Test fun placeholder() {
            expect(true) { TextField("name").apply { placeholder = "the name" } .matches { withPlaceholder("the name") } }
            expect(true) { PasswordField("password").apply { placeholder = "at least 6 characters" }.matches { withPlaceholder("at least 6 characters") } }
            expect(true) { ComboBox<String>().apply { placeholder = "foo" }.matches { withPlaceholder("foo") } }
            expect(false) { TextField("name").apply { placeholder = "the name" } .matches { withPlaceholder("name") } }
            expect(false) { PasswordField("password").apply { placeholder = "at least 6 characters" }.matches { withPlaceholder("password") } }
        }
        @Test fun value() {
            expect(true) { TextField(null, "Mannerheim", "placeholder").matches { withValue("Mannerheim") } }
            expect(false) { TextField("Mannerheim").matches { withValue("Mannerheim") } }
        }
        @Test fun styles() {
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { } }
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { withClasses("a") } }
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { withClasses("b") } }
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { withClasses("a b") } }
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { withClasses("a   b") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withClasses("a c") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withClasses("a   c") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withClasses("c") } }
        }
        @Test fun withoutStyles() {
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("a") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("b") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("a b") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("a   b") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("a c") } }
            expect(false) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("a   c") } }
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("c") } }
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("c d") } }
            expect(true) { Button().apply { addClassNames("a", "b") } .matches { withoutClasses("c   d") } }
        }
        @Test fun predicates() {
            expect(true) { Button().matches {}}
            expect(false) { Button().matches { withPredicate { false } }}
        }
    }

    @Test fun `_assertNoDialogs() smoke`() {
        LocatorJ._assertNoDialogs()
    }
}
