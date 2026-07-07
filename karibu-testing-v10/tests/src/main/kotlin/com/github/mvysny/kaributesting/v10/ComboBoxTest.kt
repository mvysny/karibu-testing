package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.data.renderer.ComponentRenderer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractComboBoxTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Nested inner class ComboBoxTests {
        @Nested inner class getSuggestionItems {
            @Test fun `by default shows all items`() {
                val cb = ComboBox<String>().apply {
                    setItems2(listOf("aaa", "bbb", "ccc"))
                }
                expectList("aaa", "bbb", "ccc") { cb.getSuggestionItems() }
            }

            @Test fun `setting user input filters out stuff`() {
                val cb = ComboBox<String>().apply {
                    setItems2(listOf("aaa", "bbb", "ccc"))
                }
                cb.setUserInput("a")
                expectList("aaa") { cb.getSuggestionItems() }
            }

            @Test fun `full-blown example`() {
                val cb = ComboBox<TestPerson>().apply {
                    setItems2((0..10).map { TestPerson("foo $it", it) })
                    setItemLabelGenerator { it.name }
                }
                cb.setUserInput("foo 1")
                expectList("foo 1", "foo 10") { cb.getSuggestions() }
            }
        }
        @Nested inner class selectByLabel {
            abstract inner class withBypassSetUserInput(val bypassSetUserInput: Boolean) {
                @Test fun simple() {
                    val cb = ComboBox<String>().apply {
                        setItems2(listOf("aaa", "bbb", "ccc"))
                    }
                    cb.selectByLabel("aaa")
                    expect("aaa") { cb._value }
                }
                @Test fun `fails on no match`() {
                    val cb = ComboBox<String>().apply {
                        setItems2(listOf("aaa", "bbb", "ccc"))
                    }
                    expectThrows<AssertionError>("ComboBox[value='null', dataprovider='ListDataProvider<String>(3 items)']: No item found with label 'd'. Available items: ['aaa'=>aaa, 'bbb'=>bbb, 'ccc'=>ccc]") {
                        cb.selectByLabel("d")
                    }
                }
                @Test fun `fails on multiple match`() {
                    val cb = ComboBox<String>().apply {
                        setItems2(listOf("aaa", "aaa", "ccc"))
                    }
                    expectThrows<AssertionError>("ComboBox[value='null', dataprovider='ListDataProvider<String>(3 items)']: Multiple items found with label 'aaa': [aaa, aaa]") {
                        cb.selectByLabel("aaa")
                    }
                }
            }

            @Nested inner class WithoutBypassSetUserInput : withBypassSetUserInput(false)
            @Nested inner class WithBypassSetUserInput : withBypassSetUserInput(true)
        }
        @Nested inner class _fireCustomValueSet {
            @Test fun smoke() {
                val cb = ComboBox<String>().apply {
                    setItems2(listOf("aaa", "bbb", "ccc"))
                    isAllowCustomValue = true
                }
                cb._fireCustomValueSet("ddd")
            }
        }
    }

    @Nested inner class SelectTests {
        @Nested inner class getSuggestionItems {
            @Test fun `simple strings`() {
                val cb = Select<String>().apply {
                    setItems2(listOf("aaa", "bbb", "ccc"))
                }
                expectList("aaa", "bbb", "ccc") { cb.getSuggestionItems() }
                expectList("aaa", "bbb", "ccc") { cb.getSuggestions() }
            }

            @Test fun `full-blown example`() {
                val cb = Select<TestPerson>().apply {
                    setItems2((0..5).map { TestPerson("foo $it", it) })
                    setItemLabelGenerator { it.name }
                }
                expectList("foo 0", "foo 1", "foo 2", "foo 3", "foo 4", "foo 5") { cb.getSuggestions() }
            }
        }
        @Nested inner class selectByLabel {
            @Test fun simple() {
                val cb = Select<String>().apply {
                    setItems2(listOf("aaa", "bbb", "ccc"))
                }
                cb.selectByLabel("aaa")
                expect("aaa") { cb._value }
            }
            @Test fun `fails on no match`() {
                val cb = Select<String>().apply {
                    setItems2(listOf("aaa", "bbb", "ccc"))
                }
                expectThrows<AssertionError>("Select[value='null', dataprovider='ListDataProvider<String>(3 items)']: No item found with label 'd'. Available items: ['aaa'=>aaa, 'bbb'=>bbb, 'ccc'=>ccc]") {
                    cb.selectByLabel("d")
                }
            }
            @Test fun `fails on multiple match`() {
                val cb = Select<String>().apply {
                    setItems2(listOf("aaa", "aaa", "ccc"))
                }
                expectThrows<AssertionError>("Select[value='null', dataprovider='ListDataProvider<String>(3 items)']: Multiple items found with label 'aaa': [aaa, aaa]") {
                    cb.selectByLabel("aaa")
                }
            }
        }
    }
    @Nested inner class _getRenderedComponentFor {
        @Test fun ComboBoxTests() {
            val cb = ComboBox<String>().apply {
                setRenderer(ComponentRenderer { it -> Span(it) })
            }
            expect("foo") { (cb._getRenderedComponentFor("foo") as Span).text }
        }
        @Test fun SelectTests() {
            val cb = Select<String>().apply {
                setRenderer(ComponentRenderer { it -> Span(it) })
            }
            expect("foo") { (cb._getRenderedComponentFor("foo") as Span).text }
        }
    }
}

fun <T> ComboBox<T>.setItems2(items: Collection<T>) {
    setItems(items)
}

fun <T> Select<T>.setItems2(items: Collection<T>) {
    setItems(items)
}
