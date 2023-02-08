package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectList
import com.github.mvysny.dynatest.expectThrows
import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.provider.ListDataProvider
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.comboBoxTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    group("ComboBox") {
        group("getSuggestionItems()") {
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
        group("selectByLabel") {
            fun withBypassSetUserInput(bypassSetUserInput: Boolean) {
                group("bypassSetUserInput=$bypassSetUserInput") {
                    test("simple") {
                        val cb = ComboBox<String>().apply {
                            setItems2(listOf("aaa", "bbb", "ccc"))
                        }
                        cb.selectByLabel("aaa")
                        expect("aaa") { cb._value }
                    }
                    test("fails on no match") {
                        val cb = ComboBox<String>().apply {
                            setItems2(listOf("aaa", "bbb", "ccc"))
                        }
                        expectThrows<AssertionError>("ComboBox[value='null', dataprovider='ListDataProvider<String>(3 items)']: No item found with label 'd'. Available items: ['aaa'=>aaa, 'bbb'=>bbb, 'ccc'=>ccc]") {
                            cb.selectByLabel("d")
                        }
                    }
                    test("fails on multiple match") {
                        val cb = ComboBox<String>().apply {
                            setItems2(listOf("aaa", "aaa", "ccc"))
                        }
                        expectThrows<AssertionError>("ComboBox[value='null', dataprovider='ListDataProvider<String>(3 items)']: Multiple items found with label 'aaa': [aaa, aaa]") {
                            cb.selectByLabel("aaa")
                        }
                    }
                }
            }

            withBypassSetUserInput(false)
            withBypassSetUserInput(true)
        }
    }

    group("Select") {
        group("getSuggestionItems()") {
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
        group("selectByLabel") {
            test("simple") {
                val cb = Select<String>().apply {
                    setItems2(listOf("aaa", "bbb", "ccc"))
                }
                cb.selectByLabel("aaa")
                expect("aaa") { cb._value }
            }
            test("fails on no match") {
                val cb = Select<String>().apply {
                    setItems2(listOf("aaa", "bbb", "ccc"))
                }
                expectThrows<AssertionError>("Select[value='null', dataprovider='ListDataProvider<String>(3 items)']: No item found with label 'd'. Available items: ['aaa'=>aaa, 'bbb'=>bbb, 'ccc'=>ccc]") {
                    cb.selectByLabel("d")
                }
            }
            test("fails on multiple match") {
                val cb = Select<String>().apply {
                    setItems2(listOf("aaa", "aaa", "ccc"))
                }
                expectThrows<AssertionError>("Select[value='null', dataprovider='ListDataProvider<String>(3 items)']: Multiple items found with label 'aaa': [aaa, aaa]") {
                    cb.selectByLabel("aaa")
                }
            }
        }
    }
}

fun <T> ComboBox<T>.setItems2(items: Collection<T>) {
    setItems(items)
}

fun <T> Select<T>.setItems2(items: Collection<T>) {
    setItems(items)
}
