package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v8.button
import com.github.mvysny.karibudsl.v8.label
import com.github.mvysny.karibudsl.v8.textField
import com.github.mvysny.karibudsl.v8.verticalLayout
import com.vaadin.server.Page
import com.vaadin.ui.*
import java.lang.NullPointerException
import kotlin.test.expect

class LocatorTest : DynaTest({

    beforeEach { MockVaadin.setup() }
    beforeEach { testingLifecycleHook = MyLifecycleHook() }
    afterEach { testingLifecycleHook = TestingLifecycleHook.default }
    afterEach { MockVaadin.tearDown() }

    group("_get") {
        test("FailsOnNoComponents UI") {
            expectThrows(AssertionError::class) {
                _get(Label::class.java)
            }
            expectAfterLookupCalled()
        }

        test("FailsOnNoComponents") {
            expectThrows(AssertionError::class) {
                Button()._get(Label::class.java)
            }
            expectAfterLookupCalled()
        }

        test("fails when multiple components match") {
            expectThrows(AssertionError::class) {
                UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }._get(VerticalLayout::class.java)
            }
            expectAfterLookupCalled()
        }

        test("selects self") {
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

    group("_expectOne") {
        test("FailsOnNoComponents UI") {
            expectThrows(AssertionError::class) {
                _expectOne(Label::class.java)
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

    group("_find") {
        test("findMatchingId") {
            val button = Button().apply { id = "foo" }
            expect(listOf(button)) { VerticalLayout(button, Button())._find<Button> { id = "foo" } }
            expectAfterLookupCalled()
        }
    }

    group("_expectNone") {
        test("succeeds on no matched components") {
            Button()._expectNone(Label::class.java)
            expectAfterLookupCalled()
        }

        test("fails when multiple components match") {
            expectThrows(AssertionError::class) {
                UI.getCurrent().verticalLayout {
                    verticalLayout { }
                }._expectNone(VerticalLayout::class.java)
            }
            expectAfterLookupCalled()
        }

        test("selects self") {
            expectThrows(AssertionError::class) { Button()._expectNone(Button::class.java) }
            expectAfterLookupCalled()
        }

        test("ReturnsNested") {
            expectThrows(AssertionError::class) { VerticalLayout(Button())._expectNone(Button::class.java) }
            expectAfterLookupCalled()
        }
    }

    test("simpleUITest") {
        lateinit var layout: VerticalLayout
        layout = UI.getCurrent().verticalLayout {
            val name = textField {
                caption = "Type your name here:"
            }
            button("Click Me") {
                addClickListener {
                    println("Thanks ${name.value}, it works!")
                    layout.label("Thanks ${name.value}, it works!")
                }
            }
        }

        _get<TextField> { caption = "Type your name here:" }.value = "Baron Vladimir Harkonnen"
        expectAfterLookupCalled()
        _get<Button> { caption = "Click Me" }._click()
        expectAfterLookupCalled()
        expect("Thanks Baron Vladimir Harkonnen, it works!") { _get<Label>().value }
        expectAfterLookupCalled()
        _get<Label> { value = "Thanks Baron Vladimir Harkonnen, it works!" }  // test lookup by value
        expectAfterLookupCalled()
        expect("Thanks Baron Vladimir Harkonnen, it works!") { (layout.last() as Label).value }
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
            expect(true) { TextField("name").apply { placeholder = "the name" } .matches { placeholder = "the name" } }
            expect(true) { PasswordField("password").apply { placeholder = "at least 6 characters" }.matches { placeholder = "at least 6 characters" } }
            expect(true) { ComboBox<String>().apply { placeholder = "foo" }.matches { placeholder = "foo" } }
            expect(false) { TextField("name").apply { placeholder = "the name" } .matches { placeholder = "name" } }
            expect(false) { PasswordField("password").apply { placeholder = "at least 6 characters" }.matches { placeholder = "password" } }
        }
        test("value") {
            expect(true) { TextField(null, "Mannerheim").matches { value = "Mannerheim" } }
            expect(false) { TextField("Mannerheim").matches { value = "Mannerheim" } }
            expect(true) { Label("Mannerheim").matches { value = "Mannerheim" } }
            expect(false) { Label().apply { caption = "Mannerheim" }.matches { value = "Mannerheim" } }
        }
    }

    group("unmocked env") {
        beforeEach { MockVaadin.tearDown(); testingLifecycleHook = TestingLifecycleHook.default }
        test("lookup functions should work in unmocked environment") {
            Button()._get(Button::class.java)
            expectThrows(AssertionError::class, "/?: No visible TextField in Button[] matching TextField") {
                Button()._get(TextField::class.java)
            }
        }
        test("incorrectly mocked env") {
            val ui = MockUI()
            UI.setCurrent(ui)
            expectThrows(NullPointerException::class) {
                Page.getCurrent().location
            }
            // the Page has been mocked improperly, but _get should still fail with a proper error message.
            expectThrows(AssertionError::class, "/?: No visible TextField in Button[] matching TextField") {
                Button()._get(TextField::class.java)
            }
        }
    }
})

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

private fun expectAfterLookupCalled() {
    expect(MyLifecycleHook(true, true)) { testingLifecycleHook }
    testingLifecycleHook = MyLifecycleHook()
}
