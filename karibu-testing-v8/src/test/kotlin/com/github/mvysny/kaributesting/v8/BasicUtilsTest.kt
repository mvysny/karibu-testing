package com.github.mvysny.kaributesting.v8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v8.button
import com.github.mvysny.karibudsl.v8.checkBox
import com.vaadin.ui.Button
import com.vaadin.ui.CheckBox
import com.vaadin.ui.VerticalLayout
import kotlin.test.expect
import kotlin.test.fail

class BasicUtilsTest : DynaTest({
    group("HasValue.setValue()") {
        test("enabled check box") {
            expect(true) { CheckBox().apply { value = true } .value }
            expect(true) { CheckBox().apply { _value = true } .value }
        }

        test("disabled check box") {
            // Vaadin ignores the enabled flag and updates the value happily.
            expect(true) { CheckBox().apply { isEnabled = false; value = true } .value }
            // However, calling _value will fail
            val cb = CheckBox().apply { isEnabled = false }
            expectThrows(IllegalStateException::class, "The CheckBox[DISABLED, value='false'] is not enabled") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("invisible check box") {
            expect(true) { CheckBox().apply { isVisible = false; value = true } .value }
            // However, calling _value will fail
            val cb = CheckBox().apply { isVisible = false }
            expectThrows(IllegalStateException::class, "The CheckBox[INVIS, value='false'] is not effectively visible") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("check box with parent disabled") {
            val layout = VerticalLayout().apply { isEnabled = false }
            // Vaadin ignores the enabled flag and updates the value happily.
            expect(true) { layout.checkBox { value = true } .value }
            // However, calling _value will fail
            val cb = layout.checkBox()
            expectThrows(IllegalStateException::class, "The CheckBox[value='false'] is nested in a disabled component") {
                cb._value = true
            }
            expect(false) { cb.value }
        }

        test("read-only check box") {
            var cb = CheckBox().apply { isReadOnly = true }
            // surprisingly this works too. I'm pretty sure it failed with ReadOnlyException in Vaadin 7...
            cb.value = true
            expect(true) { cb.value }

            cb = CheckBox().apply { isReadOnly = true }
            expectThrows(IllegalStateException::class, "The CheckBox[RO, value='false'] is read-only") {
                cb._value = true
            }
            expect(false) { cb.value }
        }
    }
})
