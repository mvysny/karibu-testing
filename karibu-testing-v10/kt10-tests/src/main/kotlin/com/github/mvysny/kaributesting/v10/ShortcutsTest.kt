package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.kaributools.ModifierKey.Alt
import com.github.mvysny.kaributools.addClickShortcut
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.KeyModifier
import com.vaadin.flow.component.Shortcuts
import com.vaadin.flow.server.Command
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.shortcutsTestBatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("smoke") {
        fireShortcut(Key.ENTER) // nothing should happen
    }

    group("Button.addClickShortcut()") {
        test("simple") {
            var clicked = false
            currentUI.button {
                onClick { clicked = true }
                addClickShortcut(Key.ENTER)
            }
            fireShortcut(Key.ENTER)
            expect(true) { clicked }
        }

        test("button not triggered on different key press") {
            var clicked = false
            currentUI.button {
                onClick { clicked = true }
                addClickShortcut(Key.KEY_A)
            }
            fireShortcut(Key.ENTER)
            expect(false) { clicked }
            fireShortcut(Key.SPACE)
            expect(false) { clicked }
        }

        test("button not triggered on different modifiers") {
            var clicked = false
            currentUI.button {
                onClick { clicked = true }
                addClickShortcut(Alt + Key.KEY_A)
            }
            fireShortcut(Key.KEY_A)
            expect(false) { clicked }
            fireShortcut(Key.KEY_A, Key.CONTROL)
            expect(false) { clicked }
            fireShortcut(Key.KEY_A, Key.CONTROL, Key.ALT)
            expect(false) { clicked }
            fireShortcut(Key.KEY_A, Key.ALT)
            expect(true) { clicked }
        }

        test("space") {
            // Key.SPACE has multiple key bindings, test that out.
            var clicked = false
            currentUI.button {
                onClick { clicked = true }
                addClickShortcut(Key.SPACE)
            }
            fireShortcut(Key.SPACE)
            expect(true) { clicked }
        }

        test("space not triggered on different modifiers") {
            var clicked = false
            currentUI.button {
                onClick { clicked = true }
                addClickShortcut(Alt + Key.SPACE)
            }
            fireShortcut(Key.ENTER)
            expect(false) { clicked }
            fireShortcut(Key.SPACE)
            expect(false) { clicked }
            fireShortcut(Key.SPACE, Key.CONTROL)
            expect(false) { clicked }
            fireShortcut(Key.SPACE, Key.CONTROL, Key.ALT)
            expect(false) { clicked }
            fireShortcut(Key.SPACE, Key.ALT)
            expect(true) { clicked }
        }
    }

    group("Shortcuts.addShortcutListener()") {
        test("simple") {
            var clicked = false
            Shortcuts.addShortcutListener(currentUI, Command { clicked = true }, Key.ENTER)
            fireShortcut(Key.ENTER)
            expect(true) { clicked }
        }

        test("button not triggered on different key press") {
            var clicked = false
            Shortcuts.addShortcutListener(currentUI, Command { clicked = true }, Key.KEY_A)
            fireShortcut(Key.ENTER)
            expect(false) { clicked }
            fireShortcut(Key.SPACE)
            expect(false) { clicked }
        }

        test("button not triggered on different modifiers") {
            var clicked = false
            Shortcuts.addShortcutListener(currentUI, Command { clicked = true }, Key.KEY_A, KeyModifier.entries[2] /*ALT*/)
            fireShortcut(Key.KEY_A)
            expect(false) { clicked }
            fireShortcut(Key.KEY_A, Key.CONTROL)
            expect(false) { clicked }
            fireShortcut(Key.KEY_A, Key.CONTROL, Key.ALT)
            expect(false) { clicked }
            fireShortcut(Key.KEY_A, Key.ALT)
            expect(true) { clicked }
        }

        test("space") {
            // Key.SPACE has multiple key bindings, test that out.
            var clicked = false
            Shortcuts.addShortcutListener(currentUI, Command { clicked = true }, Key.SPACE)
            fireShortcut(Key.SPACE)
            expect(true) { clicked }
        }

        test("space not triggered on different modifiers") {
            var clicked = false
            Shortcuts.addShortcutListener(currentUI, Command { clicked = true }, Key.SPACE, KeyModifier.entries[2] /*ALT*/)
            fireShortcut(Key.ENTER)
            expect(false) { clicked }
            fireShortcut(Key.SPACE)
            expect(false) { clicked }
            fireShortcut(Key.SPACE, Key.CONTROL)
            expect(false) { clicked }
            fireShortcut(Key.SPACE, Key.CONTROL, Key.ALT)
            expect(false) { clicked }
            fireShortcut(Key.SPACE, Key.ALT)
            expect(true) { clicked }
        }
    }
}
