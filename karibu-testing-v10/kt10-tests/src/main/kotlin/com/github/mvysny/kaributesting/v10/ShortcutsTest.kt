package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.karibudsl.v10.ModifierKey.Alt
import com.github.mvysny.karibudsl.v10.addClickShortcut
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.KeyModifier
import kotlin.test.expect

/**
 * @author Martin Vysny <mavi@vaadin.com>
 */
internal fun DynaNodeGroup.shortcutsTestBatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("smoke") {
        fireShortcut(Key.ENTER) // nothing should happen
    }

    test("simple") {
        var clicked = false
        currentUI.button {
            onLeftClick { clicked = true }
            addClickShortcut(Key.ENTER)
        }
        fireShortcut(Key.ENTER)
        expect(true) { clicked }
    }

    test("button not triggered on different key press") {
        var clicked = false
        currentUI.button {
            onLeftClick { clicked = true }
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
            onLeftClick { clicked = true }
            addClickShortcut(Alt + Key.KEY_A)
        }
        fireShortcut(Key.KEY_A)
        expect(false) { clicked }
        fireShortcut(Key.KEY_A, KeyModifier.CONTROL)
        expect(false) { clicked }
        fireShortcut(Key.KEY_A, KeyModifier.CONTROL, KeyModifier.ALT)
        expect(false) { clicked }
        fireShortcut(Key.KEY_A, KeyModifier.ALT)
        expect(true) { clicked }
    }

    test("space") {
        // Key.SPACE has multiple key bindings, test that out.
        var clicked = false
        currentUI.button {
            onLeftClick { clicked = true }
            addClickShortcut(Key.SPACE)
        }
        fireShortcut(Key.SPACE)
        expect(true) { clicked }
    }

    test("space not triggered on different modifiers") {
        var clicked = false
        currentUI.button {
            onLeftClick { clicked = true }
            addClickShortcut(Alt + Key.SPACE)
        }
        fireShortcut(Key.ENTER)
        expect(false) { clicked }
        fireShortcut(Key.SPACE)
        expect(false) { clicked }
        fireShortcut(Key.SPACE, KeyModifier.CONTROL)
        expect(false) { clicked }
        fireShortcut(Key.SPACE, KeyModifier.CONTROL, KeyModifier.ALT)
        expect(false) { clicked }
        fireShortcut(Key.SPACE, KeyModifier.ALT)
        expect(true) { clicked }
    }
}
