package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import kotlin.streams.asSequence
import kotlin.test.expect

internal fun DynaNodeGroup.locatorTest() {

    beforeEach { MockVaadin.setup() }
    beforeEach { testingLifecycleHook = MyLifecycleHook() }
    afterEach { testingLifecycleHook = TestingLifecycleHook.noop }
    afterEach { MockVaadin.tearDown() }

    group("_get") {
        test("fails when no component match") {
            expectThrows(AssertionError::class) {
                Button()._get(TextField::class.java)
            }
            expectAfterLookupCalled()
        }

        test("fail when multiple component match") {
            expectThrows(AssertionError::class) {
                UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }._get(VerticalLayout::class.java)
            }
            expectAfterLookupCalled()
        }

        test("ReturnsSelf") {
            val button = Button()
            expect(button) { button._get(Button::class.java) }
            expectAfterLookupCalled()
        }

        test("ReturnsNested") {
            val button = Button()
            expect(button) { VerticalLayout(button)._get(Button::class.java) }
            expectAfterLookupCalled()
        }
    }

    group("_find") {
        test("findMatchingId") {
            val button = Button().apply { id_ = "foo" }
            expect(listOf(button)) { VerticalLayout(button, Button())._find<Button> { id = "foo" } }
            expectAfterLookupCalled()
        }
    }

    group("_expectNone") {
        test("succeeds when no component match") {
            Button()._expectNone<TextField>()
            expectAfterLookupCalled()
        }

        test("fail when multiple component match") {
            expectThrows(AssertionError::class) {
                UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }._expectNone<VerticalLayout>()
                expectAfterLookupCalled()
            }
        }

        test("fails if self matches") {
            val button = Button()
            expectThrows(AssertionError::class) { button._expectNone<Button>() }
            expectAfterLookupCalled()
        }

        test("fails if nested matches") {
            val button = Button()
            expectThrows(AssertionError::class) { VerticalLayout(button)._expectNone<Button>() }
            expectAfterLookupCalled()
        }
    }

    test("simpleUITest") {
        lateinit var layout: VerticalLayout
        layout = UI.getCurrent().verticalLayout {
            val name = textField("Type your name here:")
            button("Click Me") {
                addClickListener {
                    println("Thanks ${name.value}, it works!")
                    layout.text("Thanks ${name.value}, it works!")
                }
            }
        }

        _get<TextField> { caption = "Type your name here:" }.value = "Baron Vladimir Harkonnen"
        expectAfterLookupCalled()
        _get<Button> { caption = "Click Me" }._click()
        expectAfterLookupCalled()
        expect("Thanks Baron Vladimir Harkonnen, it works!") { _get<Text>().text }
        expectAfterLookupCalled()
        _get<TextField> { value = "Baron Vladimir Harkonnen" }  // test lookup by value
        expectAfterLookupCalled()
        expect("Thanks Baron Vladimir Harkonnen, it works!") { (layout.children.asSequence().last() as Text).text }
        expect(3) { layout.componentCount }
    }

    group("matcher") {
        fun Component.matches(spec: SearchSpec<Component>.()->Unit): Boolean = SearchSpec(Component::class.java).apply { spec() }.toPredicate().invoke(this)
        test("caption") {
            expect(true) { Button("click me").matches { caption = "click me" } }
            expect(true) { TextField("name:").matches { caption = "name:" } }
            expect(true) { Button("click me").matches { } }
            expect(true) { TextField("name:").matches { } }
            expect(false) { Button("click me").matches { caption = "Click Me" } }
            expect(false) { TextField("name:").matches { caption = "Name"} }
        }
        test("placeholder") {
            expect(true) { TextField("name", "the name").matches { placeholder = "the name" } }
            expect(true) { PasswordField("password", "at least 6 characters").matches { placeholder = "at least 6 characters" } }
            expect(true) { ComboBox<String>().apply { placeholder = "foo" }.matches { placeholder = "foo" } }
            expect(false) { TextField("name", "the name").matches { placeholder = "name" } }
            expect(false) { PasswordField("password", "at least 6 characters").matches { placeholder = "password" } }
        }
        test("value") {
            expect(true) { TextField(null, "Mannerheim", "placeholder").matches { value = "Mannerheim" } }
            expect(false) { TextField("Mannerheim").matches { value = "Mannerheim" } }
        }
    }

    group("unmocked env") {
        beforeEach { MockVaadin.tearDown(); testingLifecycleHook = TestingLifecycleHook.noop }
        test("lookup functions should work in unmocked environment") {
            Button()._get(Button::class.java)
            expectThrows(AssertionError::class) {
                Button()._get(TextField::class.java)
            }
        }
    }

    group("_expectOne") {
        test("FailsOnNoComponents UI") {
            expectThrows(AssertionError::class) {
                _expectOne(Button::class.java)
            }
            expectAfterLookupCalled()
        }

        test("FailsOnNoComponents") {
            expectThrows(AssertionError::class) {
                Button()._expectOne(Label::class.java)
            }
            expectAfterLookupCalled()
        }

        test("fails when multiple components match") {
            expectThrows(AssertionError::class) {
                UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }._expectOne(VerticalLayout::class.java)
            }
            expectAfterLookupCalled()
        }

        test("selects self") {
            Button()._expectOne(Button::class.java)
            expectAfterLookupCalled()
        }

        test("returns nested") {
            VerticalLayout(Button())._expectOne(Button::class.java)
            expectAfterLookupCalled()
        }

        test("spec") {
            expectThrows(AssertionError::class) {
                Button("foo")._expectOne<Button> { caption = "bar" }
            }
            expectAfterLookupCalled()
            expectThrows(AssertionError::class) {
                Button("foo")._expectOne(Button::class.java) { caption = "bar" }
            }
        }
    }

    group("_expect") {
        test("FailsOnNoComponents UI") {
            expectThrows(AssertionError::class) { _expect<Label>() }
            expectAfterLookupCalled()
        }

        test("FailsOnNoComponents") {
            expectThrows(AssertionError::class) { Button()._expect<Label>() }
            expectAfterLookupCalled()
        }

        test("matching 0 components works") {
            _expect<Label>(0)
            expectAfterLookupCalled()
            Button()._expect<Label>(0)
        }

        test("fails when the count is wrong") {
            expectThrows(AssertionError::class) {
                UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }._expect<VerticalLayout>()
            }
            expectAfterLookupCalled()
        }

        test("succeeds when the count is right") {
            UI.getCurrent().verticalLayout {
                verticalLayout { }
            }._expect<VerticalLayout>(2)
            expectAfterLookupCalled()
        }

        test("selects self") {
            Button()._expect<Button>(1)
            expectAfterLookupCalled()
        }

        test("returns nested") {
            VerticalLayout(Button())._expect<Button>(1)
            expectAfterLookupCalled()
        }

        test("spec") {
            expectThrows(AssertionError::class) {
                Button("foo")._expect<Button> { caption = "bar" }
            }
            expectAfterLookupCalled()
            expectThrows(AssertionError::class) {
                Button("foo")._expect(Button::class.java) { caption = "bar" }
            }
        }
    }
}

data class MyLifecycleHook(var isBeforeLookupCalled: Boolean = false, var isAfterLookupCalled: Boolean = false) : TestingLifecycleHook {
    override fun awaitBeforeLookup() {
        check(!isBeforeLookupCalled) { "awaitBeforeLookup() has been already called" }
        check(!isAfterLookupCalled) { "awaitAfterLookup() has been already called" }
        isBeforeLookupCalled = true
    }

    override fun awaitAfterLookup() {
        check(isBeforeLookupCalled) { "awaitBeforeLookup() has not yet been called" }
        check(!isAfterLookupCalled) { "awaitAfterLookup() has been already called" }
        isAfterLookupCalled = true
    }
}

fun expectAfterLookupCalled() {
    expect(MyLifecycleHook(true, true)) { testingLifecycleHook }
    testingLifecycleHook = MyLifecycleHook()
}
