package com.github.karibu.testing

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route
import kotlin.test.expect

class BasicUtilsTest : DynaTest({

    val allViews = setOf(TestingView::class.java, HelloWorldView::class.java, WelcomeView::class.java)

    test("AutoViewDiscovery") {
        expect(allViews) { autoDiscoverViews("com.github") }
    }

    test("calling autoDiscoverViews() multiple times won't fail") {
        expect(allViews) { autoDiscoverViews("com.github") }
        expect(allViews) { autoDiscoverViews("com.github") }
    }

    group("button click") {
        fun expectClickCount(button: Button, clickCount: Int, block: Button.()->Unit) {
            var clicked = 0
            button.addClickListener { if (++clicked > clickCount) kotlin.test.fail("Clicked more than $clickCount times") }
            button.block()
            expect(clickCount) { clicked }
        }

        test("enabled button") {
            // click() does nothing without an actual Browser, bummer
            expectClickCount(Button(), 0) { click() }
            expectClickCount(Button(), 1) { _click() }
        }

        test("disabled button") {
            // click() does nothing without an actual Browser, bummer
            expectClickCount(Button().apply { isEnabled = false }, 0) { click() }
            // however _click() will properly fail
            expectThrows(IllegalStateException::class, "The Button[DISABLED] is not enabled") {
                expectClickCount(Button().apply { isEnabled = false }, 0) { _click() }
            }
        }
    }
})

@Route("testing")
class TestingView : Div()
