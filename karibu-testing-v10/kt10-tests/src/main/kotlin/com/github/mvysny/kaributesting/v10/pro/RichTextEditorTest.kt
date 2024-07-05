package com.github.mvysny.kaributesting.v10.pro

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.vaadin.flow.component.richtexteditor.RichTextEditor
import kotlin.test.expect

/**
 * In Vaadin 23, RichTextEditor is a simple field with a Delta String value. When binding to HTML value, you need to use [RichTextEditor.asHtml].
 * This test tests that everything works as expected.
 */
@DynaTestDsl
internal fun DynaNodeGroup.richTextEditorTests() {

    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("smoke") {
        val cb = RichTextEditor()
        cb.asHtml().value = "foo"
        expect("foo") { cb.asHtml().value }
    }

    test("listeners fired") {
        val cb = RichTextEditor()
        var called = false
        cb.asHtml().addValueChangeListener {
            expect(false) { called }
            called = true
            expect("foo") { it.value }
        }
        cb._htmlValue = "foo"
        expect("foo") { cb._htmlValue }
        expect(true) { called }
    }

    test("disabled") {
        // Vaadin ignores the enabled flag and updates the value happily.
        expect("foo") { RichTextEditor().apply { isEnabled = false; asHtml().value = "foo" } .asHtml().value }
        // However, calling _value will fail
        val cb = RichTextEditor()
        cb.isEnabled = false
        expectThrows(IllegalStateException::class, "The RichTextEditor[DISABLED, value=''] is not enabled") {
            cb._htmlValue = "foo"
        }
        expect("") { cb._htmlValue }
    }
}
