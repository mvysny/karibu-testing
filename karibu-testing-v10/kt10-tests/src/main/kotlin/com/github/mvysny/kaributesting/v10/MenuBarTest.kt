package com.github.mvysny.kaributesting.v10

import com.github.mvysny.karibudsl.v10.item
import com.github.mvysny.karibudsl.v10.menuBar
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.menubar.MenuBar
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

abstract class AbstractMenuBarTests() {
    @BeforeEach fun fakeVaadin() { MockVaadin.setup() }
    @AfterEach fun tearDownVaadin() { MockVaadin.tearDown() }

    @Test fun `_clickItemWithCaption fires click listener`() {
        var clicked = 0
        val menuBar: MenuBar = UI.getCurrent().menuBar {
            item("foo", { clicked++ })
        }
        menuBar._clickItemWithCaption("foo")
        expect(1) { clicked }
    }

    @Test fun `_clickItemWithCaption toggles isChecked`() {
        lateinit var i: MenuItem
        val menuBar: MenuBar = UI.getCurrent().menuBar {
            item("bar") {
                i = item("foo") { isCheckable = true }
            }
        }
        menuBar._clickItemWithCaption("foo")
        expect(true) { i.isChecked }
    }

    @Test fun _click() {
        var clicked = 0
        lateinit var mi: MenuItem
        val menuBar: MenuBar = UI.getCurrent().menuBar {
            mi = item("foo", { clicked++ })
        }
        menuBar._click(mi)
        expect(1) { clicked }
    }

    // test for https://github.com/mvysny/karibu-testing/issues/76
    @Nested inner class `locate components` {
        @Test fun `in MenuBar`() {
            val menuBar: MenuBar = UI.getCurrent().menuBar {
                val mi = item(Span("foo"))
                println("$mi ${mi.children.toList()}")
            }
            menuBar._expectOne<Span>()
        }
        @Test fun `Item in submenu`() {
            val menuBar: MenuBar = UI.getCurrent().menuBar {
                item("foo") {
                    item(Span("foo"))
                }
            }
            menuBar._expectOne<Span>()
        }
        @Test fun `Component in submenu`() {
            // PopupButton in Karibu-DSL uses this trick
            // test for https://github.com/mvysny/karibu-testing/issues/163
            val menuBar: MenuBar = UI.getCurrent().menuBar {
                item("foo") {
                    subMenu.addComponent(Span("foo"))
                }
            }
            menuBar._expectOne<Span>()
        }
        @Test fun `Component in sub-submenu`() {
            // PopupButton in Karibu-DSL uses this trick
            // test for https://github.com/mvysny/karibu-testing/issues/163
            val menuBar: MenuBar = UI.getCurrent().menuBar {
                item("foo") {
                    item("bar") {
                        subMenu.addComponent(Span("foo"))
                    }
                }
            }
            menuBar._expectOne<Span>()
        }
    }
}
