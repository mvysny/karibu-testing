package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectList
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.combobox.ComboBox
import java.lang.IllegalStateException

internal fun DynaNodeGroup.comboBoxTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    if (vaadinVersion >= 12) {
        group("getSuggestionItems()") {
            test("by default shows all items") {
                val cb = ComboBox<String>().apply {
                    setItems(listOf("aaa", "bbb", "ccc"))
                }
                expectList("aaa", "bbb", "ccc") { cb.getSuggestionItems() }
            }

            test("setting user input filters out stuff") {
                val cb = ComboBox<String>().apply {
                    setItems(listOf("aaa", "bbb", "ccc"))
                }
                cb.setUserInput("a")
                expectList("aaa") { cb.getSuggestionItems() }
            }
        }
    } else {
        test("getSuggestionItems() fails with proper error message") {
            expectThrows(IllegalStateException::class, "This function only works with Vaadin 12 or higher") {
                ComboBox<String>().apply {
                    setItems(listOf("aaa", "bbb", "ccc"))
                    getSuggestionItems()
                }
            }
        }
        test("setUserInput() fails with proper error message") {
            expectThrows(IllegalStateException::class, "This function only works with Vaadin 12 or higher") {
                ComboBox<String>().apply {
                    setItems(listOf("aaa", "bbb", "ccc"))
                    setUserInput("a")
                }
            }
        }
    }
}
