package com.github.mvysny.kaributesting.v10.pro

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10._value
import com.github.mvysny.kaributesting.v10.expectThrows
import com.vaadin.flow.component.richtexteditor.RichTextEditor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.expect

/**
 * In Vaadin 24, RichTextEditor is a simple field with a HTML String value, so no special support is necessary.
 * This test only makes sure this assumption holds.
 */
abstract class AbstractRichTextEditorTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun `listeners fired`() {
        val cb = RichTextEditor()
        var called = false
        cb.addValueChangeListener {
            called = true
            expect("foo") { it.value }
        }
        cb._value = "foo"
        expect("foo") { cb._value }
        expect(true) { called }
        expect("foo") { cb.asHtml().value }
    }

    @Test fun disabled() {
        // Vaadin ignores the enabled flag and updates the value happily.
        expect("foo") { RichTextEditor().apply { isEnabled = false; value = "foo" } .asHtml().value }
        // However, calling _value will fail
        val cb = RichTextEditor()
        cb.isEnabled = false
        expectThrows<IllegalStateException>("The RichTextEditor[DISABLED, value=''] is not enabled") {
            cb._value = "foo"
        }
        expect("") { cb._value }
    }
}
