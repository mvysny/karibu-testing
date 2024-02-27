package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.karibudsl.v10.item
import com.github.mvysny.karibudsl.v10.menuBar
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.menubar.MenuBar
import kotlin.streams.toList
import kotlin.test.expect

@DynaTestDsl
internal fun DynaNodeGroup.menuBarTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("_clickItemWithCaption fires click listener") {
        var clicked = 0
        val menuBar: MenuBar = UI.getCurrent().menuBar {
            item("foo", { clicked++ })
        }
        menuBar._clickItemWithCaption("foo")
        expect(1) { clicked }
    }

    test("_clickItemWithCaption toggles isChecked") {
        lateinit var i: MenuItem
        val menuBar: MenuBar = UI.getCurrent().menuBar {
            item("bar") {
                i = item("foo") { isCheckable = true }
            }
        }
        menuBar._clickItemWithCaption("foo")
        expect(true) { i.isChecked }
    }

    test("_click") {
        var clicked = 0
        lateinit var mi: MenuItem
        val menuBar: MenuBar = UI.getCurrent().menuBar {
            mi = item("foo", { clicked++ })
        }
        menuBar._click(mi)
        expect(1) { clicked }
    }

    // test for https://github.com/mvysny/karibu-testing/issues/76
    group("locate components") {
        test("in MenuBar") {
            val menuBar: MenuBar = UI.getCurrent().menuBar {
                val mi = item(Span("foo"))
                println("$mi ${mi.children.toList()}")
            }
            menuBar._expectOne<Span>()
        }
        test("Item in submenu") {
            val menuBar: MenuBar = UI.getCurrent().menuBar {
                item("foo") {
                    item(Span("foo"))
                }
            }
            menuBar._expectOne<Span>()
        }
        test("Component in submenu") {
            // PopupButton in Karibu-DSL uses this trick
            // test for https://github.com/mvysny/karibu-testing/issues/163
            val menuBar: MenuBar = UI.getCurrent().menuBar {
                item("foo") {
                    subMenu.add(Span("foo"))
                }
            }
            menuBar._expectOne<Span>()
        }
        test("Component in sub-submenu") {
            // PopupButton in Karibu-DSL uses this trick
            // test for https://github.com/mvysny/karibu-testing/issues/163
            val menuBar: MenuBar = UI.getCurrent().menuBar {
                item("foo") {
                    item("bar") {
                        subMenu.add(Span("foo"))
                    }
                }
            }
            menuBar._expectOne<Span>()
        }
    }
}
