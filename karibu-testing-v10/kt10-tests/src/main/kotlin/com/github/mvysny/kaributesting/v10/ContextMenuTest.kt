package com.github.mvysny.kaributesting.v10

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.div
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.contextmenu.ContextMenu
import com.vaadin.flow.component.contextmenu.HasMenuItems
import com.vaadin.flow.component.contextmenu.MenuItem
import java.lang.AssertionError
import kotlin.test.expect
import kotlin.test.fail

// @todo mavi remove when karibu-dsl 0.6.0 is released

@VaadinDsl
fun (@VaadinDsl Component).contextMenu(block: ContextMenu.()->Unit = {}): ContextMenu {
    val menu = ContextMenu(this)
    menu.block()
    return menu
}

@VaadinDsl
fun (@VaadinDsl HasMenuItems).item(text: String, clickListener: ((ClickEvent<MenuItem>)->Unit)? = null,
                                   block: (@VaadinDsl MenuItem).()->Unit = {}): MenuItem =
        addItem(text, clickListener).apply { block() }

@VaadinDsl
fun (@VaadinDsl MenuItem).item(text: String, clickListener: ((ClickEvent<MenuItem>)->Unit)? = null,
                               block: (@VaadinDsl MenuItem).()->Unit = {}): MenuItem =
        subMenu.item(text, clickListener, block)

@VaadinDsl
fun (@VaadinDsl HasMenuItems).item(component: Component, clickListener: ((ClickEvent<MenuItem>)->Unit)? = null,
                                   block: (@VaadinDsl MenuItem).()->Unit = {}): MenuItem =
        addItem(component, clickListener).apply { block() }

@VaadinDsl
fun (@VaadinDsl MenuItem).item(component: Component, clickListener: ((ClickEvent<MenuItem>)->Unit)? = null,
                               block: (@VaadinDsl MenuItem).()->Unit = {}): MenuItem =
        subMenu.item(component, clickListener, block)



internal fun DynaNodeGroup.contextMenuTestbatch() {
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("simple click") {
        var clicked = 0
        lateinit var cm: ContextMenu
        UI.getCurrent().div {
            cm = contextMenu {
                item("click me", {e -> clicked++ })
            }
        }
        cm._clickItemWithCaption("click me")
        expect(1) { clicked }
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
                item("click me", {e -> fail("shouldn't be called") }) {
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
                item("click me", {e -> fail("shouldn't be called") }) {
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
                    item("click me", { e -> fail("shouldn't be called") })
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
                    item("click me", { e -> fail("shouldn't be called") })
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
                item("click me", { e -> fail("shouldn't be called") })
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
                item("click me", { e -> clicked++ })
            }
        }
        cm._clickItemWithCaption("click me")
        expect(1) { clicked }
    }
}
