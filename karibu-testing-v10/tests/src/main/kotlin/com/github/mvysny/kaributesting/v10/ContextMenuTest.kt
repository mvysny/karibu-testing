package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.IconName
import com.github.mvysny.kaributools.iconName
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.contextmenu.ContextMenu
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem
import com.vaadin.flow.component.icon.VaadinIcon
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect
import kotlin.test.fail

abstract class AbstractContextMenuTests {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun `simple click`() {
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

    @Test fun `simple click by ID`() {
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

    @Test fun `click checks isChecked`() {
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
    @Test fun `click unchecks isChecked`() {
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

    @Test fun `clicking menu item calls the 'menu open' listeners`() {
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

    @Test fun `clicking non-existent menu fails`() {
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

    @Test fun `clicking non-existent menu by ID fails`() {
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

    @Test fun `clicking disabled menu throws exception`() {
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

    @Test fun `clicking invisible menu throws exception`() {
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

    @Test fun `clicking menu with disabled parent throws exception`() {
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

    @Test fun `clicking menu with invisible parent throws exception`() {
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

    @Test fun `clicking menu on invisible component throws exception`() {
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

    @Test fun `clicking menu on disabled component succeeds`() {
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

    @Test fun `setOpened(true) fires ContextMenuBase-OpenedChangeEvent`() {
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

    @Test fun `setOpened(false) fires ContextMenuBase-OpenedChangeEvent`() {
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

    // Tests for opening/clicking a ContextMenu via its target component, without holding a
    // reference to the ContextMenu. https://github.com/mvysny/karibu-testing/issues/20
    @Nested inner class `open via target component` {
        @Test fun `_openContextMenu attaches the menu and makes it findable`() {
            lateinit var cm: ContextMenu
            val div = UI.getCurrent().div {
                cm = contextMenu {
                    item("click me")
                }
            }
            expect(0) { _find<ContextMenu>().size } // not attached until opened
            val opened: ContextMenu = div._openContextMenu()
            expect(cm) { opened }
            expect(1) { _find<ContextMenu>().size }
            expect(true) { cm.isOpened }
        }

        @Test fun `_openContextMenu fires OpenedChangeEvent`() {
            lateinit var cm: ContextMenu
            var called = false
            val div = UI.getCurrent().div {
                cm = contextMenu {
                    item("click me")
                }
            }
            cm.addOpenedChangeListener { e ->
                called = true
                expect(true) { e.isOpened }
                expect(false) { e.isFromClient }
            }
            div._openContextMenu()
            expect(true) { called }
        }

        @Test fun `_close detaches the menu`() {
            lateinit var cm: ContextMenu
            val div = UI.getCurrent().div {
                cm = contextMenu {
                    item("click me")
                }
            }
            div._openContextMenu()
            expect(1) { _find<ContextMenu>().size }
            cm._close()
            expect(0) { _find<ContextMenu>().size }
            expect(false) { cm.isOpened }
        }

        @Test fun `_openContextMenu throws when no context menu attached`() {
            val div = UI.getCurrent().div { }
            expectThrows(AssertionError::class, "No ContextMenu is attached to Div[]") {
                div._openContextMenu()
            }
        }

        @Test fun `_openContextMenu throws when multiple context menus attached`() {
            val div = UI.getCurrent().div { }
            ContextMenu(div).apply { addItem("one") }
            ContextMenu(div).apply { addItem("two") }
            expectThrows(AssertionError::class, "Multiple ContextMenus are attached to Div[]") {
                div._openContextMenu()
            }
        }

        @Test fun `click by caption clicks and auto-closes`() {
            var clicked = 0
            val div = UI.getCurrent().div {
                contextMenu {
                    item("click me", { clicked++ })
                }
            }
            div._clickContextMenuItemWithCaption("click me")
            expect(1) { clicked }
            expect(0) { _find<ContextMenu>().size } // auto-closed
        }

        @Test fun `click by ID clicks and auto-closes`() {
            var clicked = 0
            val div = UI.getCurrent().div {
                contextMenu {
                    item("click me", { clicked++ }) { setId("clickme") }
                }
            }
            div._clickContextMenuItemWithID("clickme")
            expect(1) { clicked }
            expect(0) { _find<ContextMenu>().size }
        }

        @Test fun `click by icon clicks and auto-closes`() {
            var clicked = 0
            val div = UI.getCurrent().div {
                contextMenu {
                    item(VaadinIcon.MENU.create(), { clicked++ })
                }
            }
            div._clickContextMenuItemWithIcon(IconName.of(VaadinIcon.MENU))
            expect(1) { clicked }
            expect(0) { _find<ContextMenu>().size }
        }

        @Test fun `click on disabled component succeeds`() {
            var clicked = 0
            val div = UI.getCurrent().div {
                isEnabled = false
                contextMenu {
                    item("click me", { clicked++ })
                }
            }
            div._clickContextMenuItemWithCaption("click me")
            expect(1) { clicked }
        }

        @Test fun `click on invisible component throws`() {
            val div = UI.getCurrent().div {
                isVisible = false
                contextMenu {
                    item("click me", { fail("shouldn't be called") })
                }
            }
            expectThrows(AssertionError::class, "which is not effectively visible") {
                div._clickContextMenuItemWithCaption("click me")
            }
        }

        @Test fun `clicking non-existent item fails`() {
            val div = UI.getCurrent().div {
                contextMenu {
                    item("click me")
                }
            }
            expectThrows(AssertionError::class, "No menu item with text='non-existent'") {
                div._clickContextMenuItemWithCaption("non-existent")
            }
            expect(0) { _find<ContextMenu>().size } // still auto-closed on failure
        }

        @Test fun `grid _openContextMenu populates dynamic content`() {
            lateinit var cm: GridContextMenu<String>
            val grid = UI.getCurrent().grid<String> {
                setItems2(listOf("foo", "bar"))
                cm = gridContextMenu {
                    setDynamicContentHandler { item ->
                        removeAll()
                        this.item("dynamic for $item")
                        true
                    }
                }
                _prepare()
            }
            val opened: GridContextMenu<String> = grid._openContextMenu("foo")
            expect(cm) { opened }
            expect(1) { _find<GridContextMenu<*>>().size }
            opened._clickItemWithCaption("dynamic for foo", "foo")
        }

        @Test fun `grid click by caption clicks and auto-closes`() {
            lateinit var clicked: String
            val grid = UI.getCurrent().grid<String> {
                setItems2(listOf("foo", "bar"))
                gridContextMenu {
                    item("click me", { e -> clicked = e!! })
                }
                _prepare()
            }
            grid._clickContextMenuItemWithCaption("click me", "foo")
            expect("foo") { clicked }
            expect(0) { _find<GridContextMenu<*>>().size }
        }
    }

    @Nested inner class `grid context menu` {
        @Test fun `simple click`() {
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
        @Test fun `click by non-existing caption fails`() {
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
        @Test fun `simple click by ID`() {
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
        @Test fun `click by non-existing ID fails`() {
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
        @Test fun `simple click by Icon`() {
            lateinit var clicked: String
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item(VaadinIcon.MENU.create(), { e -> clicked = e!! })
                }
            }
            // TODO replace with VaadinIcon.MENU.iconName after karibu-tools release, including val VaadinIcon.iconName
            cm._clickItemWithIcon(IconName.of(VaadinIcon.MENU), "foo")
            expect("foo") { clicked }
        }
        @Test fun `click by non-existing Icon fails`() {
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item(VaadinIcon.MENU.create(), { fail("should not be called") })
                }
            }
            expectThrows<AssertionError>(
                "No menu item with icon='vaadin:check' in GridContextMenu:\n" +
                    "└── GridContextMenu[]\n" +
                    "    └── GridMenuItem[icon='vaadin:menu']\n" +
                    "        └── Icon[icon='vaadin:menu']"
            ) {
                // TODO replace with VaadinIcon.CHECK.iconName after karibu-tools release, including val VaadinIcon.iconName
                cm._clickItemWithIcon(IconName.of(VaadinIcon.CHECK), "foo")
            }
        }
        @Test fun `click toggles isChecked`() {
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
        @Test fun `submenu click`() {
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

        @Test fun `context menu opened listener fired`() {
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
        @Test fun `dynamic menu content populated correctly`() {
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

        @Test fun `dynamic content handler vetoing open fails`() {
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("click me", { fail("should not be called") })
                    setDynamicContentHandler { false } // veto opening
                }
            }
            expectThrows(AssertionError::class, "The dynamic content handler returned false signalling the menu should not open") {
                cm._clickItemWithCaption("click me", "foo")
            }
        }

        @Test fun `setOpened(true) fires GridContextMenuOpenedEvent`() {
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

        @Test fun `setOpened(true) calls dynamic handler`() {
            var called = 0
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("click me")
                    setDynamicContentHandler { item ->
                        expect("foo") { item }
                        expect(0) { called }
                        called = 1
                        true
                    }
                }
            }
            cm.addGridContextMenuOpenedListener { e ->
                expect(1) { called }
                called = 2
            }
            cm.setOpened(true, "foo")
            expect(2) { called }
        }

        @Test fun `setOpened(true) succeeds on null item`() {
            var called = 0
            lateinit var cm: GridContextMenu<String>
            UI.getCurrent().grid<String> {
                cm = gridContextMenu {
                    item("click me")
                    setDynamicContentHandler { item ->
                        expect(null) { item }
                        expect(0) { called }
                        called = 1
                        true
                    }
                }
            }
            cm.addGridContextMenuOpenedListener { e ->
                expect(1) { called }
                called = 2
            }
            cm.setOpened(true, null)
            expect(2) { called }
        }

        @Test fun `setOpened(false) fires GridContextMenuOpenedEvent`() {
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
