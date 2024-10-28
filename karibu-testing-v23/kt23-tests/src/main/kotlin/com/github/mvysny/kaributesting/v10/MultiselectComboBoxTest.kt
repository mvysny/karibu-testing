package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributesting.v23.*
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.data.renderer.ComponentRenderer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractMultiselectComboBoxTests {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun basic() {
        val cb = MultiSelectComboBox<String>("Hello!")
        cb.setItems("a", "b", "c")
        expect(true) { (cb as Component).dataProvider != null }
        expect("MultiSelectComboBox[label='Hello!', value='[]', dataprovider='ListDataProvider<String>(3 items)']") { cb.toPrettyString() }
    }

    @Nested inner class MultiSelectComboBoxTests {
        @Nested inner class getSuggestionItems {
            @Test fun `by default shows all items`() {
                val cb = MultiSelectComboBox<String>().apply {
                    setItems(listOf("aaa", "bbb", "ccc"))
                }
                expectList("aaa", "bbb", "ccc") { cb.getSuggestionItems() }
            }

            @Test fun `setting user input filters out stuff`() {
                val cb = MultiSelectComboBox<String>().apply {
                    setItems(listOf("aaa", "bbb", "ccc"))
                }
                cb.setUserInput("a")
                expectList("aaa") { cb.getSuggestionItems() }
            }

            @Test fun `full-blown example`() {
                val cb = MultiSelectComboBox<TestPerson>().apply {
                    setItems((0..10).map { TestPerson("foo $it", it) })
                    setItemLabelGenerator { it.name }
                }
                cb.setUserInput("foo 1")
                expectList("foo 1", "foo 10") { cb.getSuggestions() }
            }
        }
        @Nested inner class selectByLabel {
            abstract inner class withBypassSetUserInput(val bypassSetUserInput: Boolean) {
                @Test fun simple() {
                    val cb = MultiSelectComboBox<String>().apply {
                        setItems(listOf("aaa", "bbb", "ccc"))
                    }
                    cb.selectByLabel("aaa")
                    expect(setOf("aaa")) { cb._value }
                }
                @Test fun `fails on no match`() {
                    val cb = MultiSelectComboBox<String>().apply {
                        setItems(listOf("aaa", "bbb", "ccc"))
                    }
                    expectThrows<AssertionError>("MultiSelectComboBox[value='[]', dataprovider='ListDataProvider<String>(3 items)']: No item found with label 'd'. Available items: ['aaa'=>aaa, 'bbb'=>bbb, 'ccc'=>ccc]") {
                        cb.selectByLabel("d")
                    }
                }
                @Test fun `fails on multiple match`() {
                    val cb = MultiSelectComboBox<String>().apply {
                        setItems(listOf("aaa", "aaa", "ccc"))
                    }
                    expectThrows<AssertionError>("MultiSelectComboBox[value='[]', dataprovider='ListDataProvider<String>(3 items)']: Multiple items found with label 'aaa': [aaa, aaa]") {
                        cb.selectByLabel("aaa")
                    }
                }
            }

            @Nested inner class WithoutBypassSetUserInput : withBypassSetUserInput(false)
            @Nested inner class WithBypassSetUserInput : withBypassSetUserInput(true)
        }
    }

    @Nested inner class _getRenderedComponentFor {
        @Test fun MultiSelectComboBox() {
            val cb = MultiSelectComboBox<String>().apply {
                setRenderer(ComponentRenderer { it -> Span(it) })
            }
            expect("foo") { (cb._getRenderedComponentFor("foo") as Span).text }
        }
    }
}
