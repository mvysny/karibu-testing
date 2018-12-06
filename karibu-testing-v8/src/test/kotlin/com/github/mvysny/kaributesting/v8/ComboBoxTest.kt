package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.karibudsl.v8.comboBox
import com.vaadin.ui.ComboBox
import com.vaadin.ui.UI

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
            UI.getCurrent().apply {
                val cb = comboBox<String> {
                    setItems(listOf("aaa", "bbb", "ccc"))
                }
                cb.setUserInput("a")
                expectList("aaa") { cb.getSuggestionItems() }
            }
        }

        test("full-blown example") {
            UI.getCurrent().apply {
                val cb = comboBox<TestPerson> {
                    setItems((0..10).map { TestPerson("foo $it", it) })
                    setItemCaptionGenerator { it.name }
                }
                cb.setUserInput("foo 1")
                expectList("foo 1", "foo 10") { cb.getSuggestions() }
            }
        }
    }
})
