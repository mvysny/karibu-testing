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

    test("simple click by ID") {
        var clicked = 0
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("click me", { clicked++ }) { setId("clickme") }
            }
        }
        cm._clickItemWithID("clickme")
        expect(1) { clicked }
    }

    test("click checks isChecked") {
        var clicked = 0
        var checkedOnClick = false
        lateinit var cm: ContextMenu
        lateinit var i: MenuItem
        UI.getCurrent().div {
            cm = contextMenu {
                i = item("click me", { clicked++; checkedOnClick = it.source.isChecked }) { isCheckable = true }
            }
        }
        cm._clickItemWithCaption("click me")
        expect(1) { clicked }
        expect(true) { i.isChecked }
        expect(true) { checkedOnClick }
    }

    // https://github.com/mvysny/karibu-testing/issues/126
    test("click unchecks isChecked") {
        var clicked = 0
        var checkedOnClick = true
        lateinit var cm: ContextMenu
        lateinit var i: MenuItem
        UI.getCurrent().div {
            cm = contextMenu {
                i = item("click me", { clicked++; checkedOnClick = it.source.isChecked }) { isCheckable = true; isChecked = true }
            }
        }
        cm._clickItemWithCaption("click me")
        expect(1) { clicked }
        expect(false) { i.isChecked }
        expect(false) { checkedOnClick }
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
        expectThrows(AssertionError::class, "No menu item with text='MenuItem[text='click me']'") {
            cm._clickItemWithCaption("MenuItem[text='click me']")
        }
    }

    test("clicking non-existent menu by ID fails") {
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("click me")
                item("foo")
            }
        }
        expectThrows(AssertionError::class, "No menu item with id='1'") {
            cm._clickItemWithID("1")
        }
    }

    test("clicking disabled menu throws exception") {
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("click me", { fail("shouldn't be called") }) {
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
                item("click me", { fail("shouldn't be called") }) {
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
                    item("click me", { fail("shouldn't be called") })
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
                    item("click me", { fail("shouldn't be called") })
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
                item("click me", { fail("shouldn't be called") })
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
                item("click me", { clicked++ })
            }
        }
        cm._clickItemWithCaption("click me")
        expect(1) { clicked }
    }

    test("setOpened(true) fires ContextMenuBase.OpenedChangeEvent") {
        var called = false
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("click me")
            }
        }
        cm.addOpenedChangeListener { e ->
            called = true
            expect(true) { e.isOpened }
            expect(false) { e.isFromClient }
        }
        cm.setOpened(true)
        expect(true) { called }
    }

    test("setOpened(false) fires ContextMenuBase.OpenedChangeEvent") {
        var called = false
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("click me")
            }
        }
        cm.setOpened(true)
        cm.addOpenedChangeListener { e ->
            called = true
            expect(false) { e.isOpened }
            expect(false) { e.isFromClient }
        }
        cm.setOpened(false)
        expect(true) { called }
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
        test("click by non-existing caption fails") {
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("click me", { fail("should not be called") })
                }
            }
            expectThrows<AssertionError>("No menu item with text='non-existing' in GridContextMenu:\n" +
                    "└── GridContextMenu[]\n" +
                    "    └── GridMenuItem[text='click me']") {
                cm._clickItemWithCaption("non-existing", "foo")
            }
        }
        test("simple click by ID") {
            lateinit var clicked: String
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("click me", { e -> clicked = e!! }) { setId("bar") }
                }
            }
            cm._clickItemWithID("bar", "foo")
            expect("foo") { clicked }
        }
        test("click by non-existing ID fails") {
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("click me", { fail("should not be called") })
                }
            }
            expectThrows<AssertionError>("No menu item with id='non-existing' in GridContextMenu:\n" +
                    "└── GridContextMenu[]\n" +
                    "    └── GridMenuItem[text='click me']") {
                cm._clickItemWithID("non-existing", "foo")
            }
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

        test("setOpened(true) fires GridContextMenuOpenedEvent") {
            var called = false
            lateinit var cm: GridContextMenu<String>
            val grid = UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("click me")
                }
                addColumn({ it }).apply {
                    id_ = "foo!"
                }
            }
            cm.addGridContextMenuOpenedListener { e ->
                called = true
                expect(true) { e.isOpened }
                expect(false) { e.isFromClient }
                expect("foo") { e.item.orElse(null) }
                expect("foo!") { e.columnId.get() }
            }
            cm.setOpened(true, "foo", grid.columns[0])
            expect(true) { called }
        }

        test("setOpened(false) fires GridContextMenuOpenedEvent") {
            var called = false
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("click me")
                }
            }
            cm.setOpened(true, "foo")
            cm.addGridContextMenuOpenedListener { e ->
                called = true
                expect(false) { e.isOpened }
                expect(false) { e.isFromClient }
                expect("foo") { e.item.orElse(null) }
                expect(null) { e.columnId.orElse(null) }
            }
            cm.setOpened(false, "foo")
            expect(true) { called }
        }
    }
}
