package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.vaadin.ui.ComboBox
import java.util.*

class ComboBoxTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("getSuggestionItems()") {
        test("by default shows all items") {
            val cb = ComboBox<String>().apply {
                setItems(listOf("aaa", "bbb", "ccc"))
            }
            expectList("aaa", "bbb", "ccc") { cb.getSuggestionItems() }
        }

        test("setting user input filters out stuff") {
            val cb = ComboBox<String>().apply {
                locale = Locale.getDefault()
                setItems(listOf("aaa", "bbb", "ccc"))
            }
            cb.setUserInput("a")
            expectList("aaa") { cb.getSuggestionItems() }
        }
    }
})
