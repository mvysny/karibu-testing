package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectList
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.select.Select

internal fun DynaNodeGroup.comboBoxTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("ComboBox.getSuggestionItems()") {
        test("by default shows all items") {
            val cb = ComboBox<String>().apply {
                setItems2(listOf("aaa", "bbb", "ccc"))
            }
            expectList("aaa", "bbb", "ccc") { cb.getSuggestionItems() }
        }

        test("setting user input filters out stuff") {
            val cb = ComboBox<String>().apply {
                setItems2(listOf("aaa", "bbb", "ccc"))
            }
            cb.setUserInput("a")
            expectList("aaa") { cb.getSuggestionItems() }
        }

        test("full-blown example") {
            val cb = ComboBox<TestPerson>().apply {
                setItems2((0..10).map { TestPerson("foo $it", it) })
                setItemLabelGenerator { it.name }
            }
            cb.setUserInput("foo 1")
            expectList("foo 1", "foo 10") { cb.getSuggestions() }
        }
    }

    group("Select.getSuggestionItems()") {
        test("simple strings") {
            val cb = Select<String>().apply {
                setItems2(listOf("aaa", "bbb", "ccc"))
            }
            expectList("aaa", "bbb", "ccc") { cb.getSuggestionItems() }
            expectList("aaa", "bbb", "ccc") { cb.getSuggestions() }
        }

        test("full-blown example") {
            val cb = Select<TestPerson>().apply {
                setItems2((0..5).map { TestPerson("foo $it", it) })
                setItemLabelGenerator { it.name }
            }
            expectList("foo 0", "foo 1", "foo 2", "foo 3", "foo 4", "foo 5") { cb.getSuggestions() }
        }
    }
}

fun <T> ComboBox<T>.setItems2(items: Collection<T>) {
    // this way it's also compatible with Vaadin 18.
    setDataProvider(ListDataProvider2(items))
}

fun <T> Select<T>.setItems2(items: Collection<T>) {
    // this way it's also compatible with Vaadin 17:
    // https://github.com/vaadin/flow/issues/8831
    dataProvider = ListDataProvider2(items)
}
