package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectList
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v23.getSuggestionItems
import com.github.mvysny.kaributesting.v23.getSuggestions
import com.github.mvysny.kaributesting.v23.selectByLabel
import com.github.mvysny.kaributesting.v23.setUserInput
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.combobox.MultiSelectComboBox
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.multiselectComboBoxTests() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("basic") {
        val cb = MultiSelectComboBox<String>("Hello!")
        cb.setItems("a", "b", "c")
        expect(true) { (cb as Component).dataProvider != null }
        expect("MultiSelectComboBox[label='Hello!', value='[]', dataprovider='ListDataProvider<String>(3 items)']") { cb.toPrettyString() }
    }

    group("MultiSelectComboBox") {
        group("getSuggestionItems()") {
            test("by default shows all items") {
                val cb = MultiSelectComboBox<String>().apply {
                    setItems(listOf("aaa", "bbb", "ccc"))
                }
                expectList("aaa", "bbb", "ccc") { cb.getSuggestionItems() }
            }

            test("setting user input filters out stuff") {
                val cb = MultiSelectComboBox<String>().apply {
                    setItems(listOf("aaa", "bbb", "ccc"))
                }
                cb.setUserInput("a")
                expectList("aaa") { cb.getSuggestionItems() }
            }

            test("full-blown example") {
                val cb = MultiSelectComboBox<TestPerson>().apply {
                    setItems((0..10).map { TestPerson("foo $it", it) })
                    setItemLabelGenerator { it.name }
                }
                cb.setUserInput("foo 1")
                expectList("foo 1", "foo 10") { cb.getSuggestions() }
            }
        }
        group("selectByLabel") {
            fun withBypassSetUserInput(bypassSetUserInput: Boolean) {
                group("bypassSetUserInput=$bypassSetUserInput") {
                    test("simple") {
                        val cb = MultiSelectComboBox<String>().apply {
                            setItems(listOf("aaa", "bbb", "ccc"))
                        }
                        cb.selectByLabel("aaa")
                        expect(setOf("aaa")) { cb._value }
                    }
                    test("fails on no match") {
                        val cb = MultiSelectComboBox<String>().apply {
                            setItems(listOf("aaa", "bbb", "ccc"))
                        }
                        expectThrows<AssertionError>("MultiSelectComboBox[value='[]', dataprovider='ListDataProvider<String>(3 items)']: No item found with label 'd'. Available items: ['aaa'=>aaa, 'bbb'=>bbb, 'ccc'=>ccc]") {
                            cb.selectByLabel("d")
                        }
                    }
                    test("fails on multiple match") {
                        val cb = MultiSelectComboBox<String>().apply {
                            setItems(listOf("aaa", "aaa", "ccc"))
                        }
                        expectThrows<AssertionError>("MultiSelectComboBox[value='[]', dataprovider='ListDataProvider<String>(3 items)']: Multiple items found with label 'aaa': [aaa, aaa]") {
                            cb.selectByLabel("aaa")
                        }
                    }
                }
            }

            withBypassSetUserInput(false)
            withBypassSetUserInput(true)
        }
    }
}
