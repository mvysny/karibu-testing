package com.github.mvysny.kaributesting.v10.pro

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10._value
import com.vaadin.flow.component.richtexteditor.RichTextEditor
import kotlin.test.expect

/**
 * In Vaadin 24, RichTextEditor is a simple field with a HTML String value, so no special support is necessary.
 * This test only makes sure this assumption holds.
 */
@DynaTestDsl
internal fun DynaNodeGroup.richTextEditorTests() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("listeners fired") {
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

    test("disabled") {
        // Vaadin ignores the enabled flag and updates the value happily.
        expect("foo") { RichTextEditor().apply { isEnabled = false; value = "foo" } .asHtml().value }
        // However, calling _value will fail
        val cb = RichTextEditor()
        cb.isEnabled = false
        expectThrows(IllegalStateException::class, "The RichTextEditor[DISABLED, value=''] is not enabled") {
            cb._value = "foo"
        }
        expect("") { cb._value }
    }
}
