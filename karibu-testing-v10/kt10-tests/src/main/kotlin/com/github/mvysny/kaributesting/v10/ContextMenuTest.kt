package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.contextmenu.ContextMenu
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem
import kotlin.test.expect
import kotlin.test.fail

@DynaTestDsl
internal fun DynaNodeGroup.contextMenuTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("simple click") {
        var clicked = 0
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("click me", { clicked++ })
            }
        }
        cm._clickItemWithCaption("click me")
        expect(1) { clicked }
    }

    test("click toggles isChecked") {
        var clicked = 0
        lateinit var cm: ContextMenu
        lateinit var i: MenuItem
        UI.getCurrent().div {
            cm = contextMenu {
                i = item("click me", { clicked++ }) { isCheckable = true }
            }
        }
        cm._clickItemWithCaption("click me")
        expect(1) { clicked }
        expect(true) { i.isChecked }
    }

    test("clicking menu item calls the 'menu open' listeners") {
        var called = false
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("click me")
            }
        }
        cm.addOpenedChangeListener { called = true }
        cm._clickItemWithCaption("click me")
        expect(true) { called }
    }

    test("clicking non-existent menu fails") {
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("click me")
                item("foo")
            }
        }
        expectThrows(AssertionError::class, "No menu item with caption MenuItem[text='click me']") {
            cm._clickItemWithCaption("MenuItem[text='click me']")
        }
    }

    test("clicking disabled menu throws exception") {
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("click me", { _ -> fail("shouldn't be called") }) {
                    isEnabled = false
                }
            }
        }
        expectThrows(AssertionError::class, "MenuItem[DISABLED, text='click me'] is not enabled") {
            cm._clickItemWithCaption("click me")
        }
    }

    test("clicking invisible menu throws exception") {
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("click me", { _ -> fail("shouldn't be called") }) {
                    isVisible = false
                }
            }
        }
        expectThrows(AssertionError::class, "MenuItem[INVIS, text='click me'] is not visible") {
            cm._clickItemWithCaption("click me")
        }
    }

    test("clicking menu with disabled parent throws exception") {
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("menu") {
                    isEnabled = false
                    item("click me", { _ -> fail("shouldn't be called") })
                }
            }
        }
        expectThrows(AssertionError::class, "MenuItem[text='click me'] is not enabled because its parent item is not enabled:") {
            cm._clickItemWithCaption("click me")
        }
    }

    test("clicking menu with invisible parent throws exception") {
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("menu") {
                    isVisible = false
                    item("click me", { _ -> fail("shouldn't be called") })
                }
            }
        }
        expectThrows(AssertionError::class, "MenuItem[text='click me'] is not visible because its parent item is not visible:") {
            cm._clickItemWithCaption("click me")
        }
    }

    test("clicking menu on invisible component throws exception") {
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            isVisible = false
            cm = contextMenu {
                item("click me", { _ -> fail("shouldn't be called") })
            }
        }
        expectThrows(AssertionError::class, "Cannot click MenuItem[text='click me'] since it's attached to Div[INVIS] which is not effectively visible") {
            cm._clickItemWithCaption("click me")
        }
    }

    test("clicking menu on disabled component succeeds") {
        var clicked = 0
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            isEnabled = false
            cm = contextMenu {
                item("click me", { _ -> clicked++ })
            }
        }
        cm._clickItemWithCaption("click me")
        expect(1) { clicked }
    }

    group("grid context menu") {
        test("simple click") {
            lateinit var clicked: String
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("click me", { e -> clicked = e!! })
                }
            }
            cm._clickItemWithCaption("click me", "foo")
            expect("foo") { clicked }
        }
        test("click toggles isChecked") {
            lateinit var i: GridMenuItem<String>
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    i = item("click me") { isCheckable = true }
                }
            }
            cm._clickItemWithCaption("click me", "foo")
            expect(true) { i.isChecked }
        }
        test("submenu click") {
            var clicked: String? = null
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("submenu") {
                        item("click me", { e -> clicked = e })
                    }
                }
            }
            cm._clickItemWithCaption("click me", "foo")
            expect("foo") { clicked }
        }

        test("context menu opened listener fired") {
            var listenerCalled = false
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    addGridContextMenuOpenedListener {
                        expect("foo") { it.item.orElse(null) }
                        listenerCalled = true
                    }
                    item("click me")
                }
            }
            cm._clickItemWithCaption("click me", "foo")
            expect(true) { listenerCalled }
        }

        // test for https://github.com/mvysny/karibu-testing/issues/40
        test("dynamic menu content populated correctly") {
            var clicked: String? = null
            var listenerCalled = false
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    setDynamicContentHandler { item ->
                        expect("foo") { item }
                        expect(false) { listenerCalled } // should only be called once
                        listenerCalled = true
                        cm.removeAll()
                        cm.item("click me", { e -> clicked = e })
                        true
                    }
                }
            }
            cm._clickItemWithCaption("click me", "foo")
            expect(true) { listenerCalled }
            expect("foo") { clicked }
        }
    }
}
