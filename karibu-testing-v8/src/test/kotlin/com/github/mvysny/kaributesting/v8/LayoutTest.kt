package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v8.label
import com.github.mvysny.karibudsl.v8.verticalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import java.lang.IllegalArgumentException
import kotlin.test.expect
import kotlin.test.fail

class LayoutTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("layout click") {
        test("simple click on child label") {
            lateinit var l: Label
            val layout = UI.getCurrent().verticalLayout {
                l = label("Click me")
            }

            var clicked = false
            layout.addLayoutClickListener {
                clicked = true
                expect(l) { it.childComponent }
                expect(l) { it.clickedComponent }
                expect(layout) { it.source }
            }
            layout._click(l)
            expect(true) { clicked }
        }

        test("click on nested label") {
            lateinit var l: Label
            lateinit var child: VerticalLayout
            val layout = UI.getCurrent().verticalLayout {
                child = verticalLayout {
                    verticalLayout {
                        l = label("Click me")
                    }
                }
            }

            var clicked = false
            layout.addLayoutClickListener {
                clicked = true
                expect(child) { it.childComponent }
                expect(l) { it.clickedComponent }
                expect(layout) { it.source }
            }
            layout._click(l)
            expect(true) { clicked }
        }

        test("clicking on label outside of the layout fails") {
            lateinit var layout: VerticalLayout
            lateinit var l: Label
            UI.getCurrent().verticalLayout {
                layout = verticalLayout()
                l = label("Click me")
            }

            layout.addLayoutClickListener { fail("Should not be called!") }
            expectThrows(IllegalArgumentException::class,
                    "The clicked component Label[value='Click me'] is not nested within this layout VerticalLayout[]") {
                layout._click(l)
            }
        }
    }
})