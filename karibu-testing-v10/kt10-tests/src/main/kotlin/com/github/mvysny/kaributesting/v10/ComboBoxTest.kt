package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectList
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.combobox.ComboBox
import java.lang.IllegalStateException

internal fun DynaNodeGroup.comboBoxTestbatch() {
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
                setItems(listOf("aaa", "bbb", "ccc"))
            }
            cb.setUserInput("a")
            expectList("aaa") { cb.getSuggestionItems() }
        }

        test("full-blown example") {
            val cb = ComboBox<TestPerson>().apply {
                setItems((0..10).map { TestPerson("foo $it", it) })
                setItemLabelGenerator { it.name }
            }
            cb.setUserInput("foo 1")
            expectList("foo 1", "foo 10") { cb.getSuggestions() }
        }
    }
}
