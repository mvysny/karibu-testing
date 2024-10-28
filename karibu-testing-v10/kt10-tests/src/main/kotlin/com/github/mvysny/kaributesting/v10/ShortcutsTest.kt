package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.onClick
import com.github.mvysny.kaributools.ModifierKey.Alt
import com.github.mvysny.kaributools.addClickShortcut
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.KeyModifier
import com.vaadin.flow.component.Shortcuts
import com.vaadin.flow.server.Command
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractShortcutsTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun smoke() {
        fireShortcut(Key.ENTER) // nothing should happen
    }

    @Nested inner class `Button-addClickShortcut()` {
        @Test fun simple() {
            var clicked = false
            currentUI.button {
                onClick { clicked = true }
                addClickShortcut(Key.ENTER)
            }
            fireShortcut(Key.ENTER)
            expect(true) { clicked }
        }

        @Test fun `button not triggered on different key press`() {
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

        @Test fun `button not triggered on different modifiers`() {
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

        @Test fun space() {
            // Key.SPACE has multiple key bindings, test that out.
            var clicked = false
            currentUI.button {
                onClick { clicked = true }
                addClickShortcut(Key.SPACE)
            }
            fireShortcut(Key.SPACE)
            expect(true) { clicked }
        }

        @Test fun `space not triggered on different modifiers`() {
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

    @Nested inner class `Shortcuts-addShortcutListener()` {
        @Test fun simple() {
            var clicked = false
            Shortcuts.addShortcutListener(currentUI, Command { clicked = true }, Key.ENTER)
            fireShortcut(Key.ENTER)
            expect(true) { clicked }
        }

        @Test fun `button not triggered on different key press`() {
            var clicked = false
            Shortcuts.addShortcutListener(currentUI, Command { clicked = true }, Key.KEY_A)
            fireShortcut(Key.ENTER)
            expect(false) { clicked }
            fireShortcut(Key.SPACE)
            expect(false) { clicked }
        }

        @Test fun `button not triggered on different modifiers`() {
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

        @Test fun space() {
            // Key.SPACE has multiple key bindings, test that out.
            var clicked = false
            Shortcuts.addShortcutListener(currentUI, Command { clicked = true }, Key.SPACE)
            fireShortcut(Key.SPACE)
            expect(true) { clicked }
        }

        @Test fun `space not triggered on different modifiers`() {
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
