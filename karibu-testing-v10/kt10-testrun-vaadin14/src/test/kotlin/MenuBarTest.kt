import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectThrows
import com.github.mvysny.karibudsl.v10.item
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10._clickItemWithCaption
import com.vaadin.flow.component.menubar.MenuBar
import kotlin.test.expect
import kotlin.test.fail

class MenuBarTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("simple click") {
        var clicked = 0
        val cm: MenuBar = MenuBar().apply {
            item("click me", { _ -> clicked++ })
        }
        cm._clickItemWithCaption("click me")
        expect(1) { clicked }
    }

    test("clicking non-existent menu fails") {
        val cm: MenuBar = MenuBar().apply {
            item("click me")
            item("foo")
        }
        expectThrows(AssertionError::class, "No menu item with caption MenuBarRootItem[text='click me']") {
            cm._clickItemWithCaption("MenuBarRootItem[text='click me']")
        }
    }

    test("clicking disabled menu throws exception") {
        val cm: MenuBar = MenuBar().apply {
            item("click me", { _ -> fail("shouldn't be called") }) {
                isEnabled = false
            }
        }
        expectThrows(AssertionError::class, "MenuBarRootItem[DISABLED, text='click me'] is not enabled") {
            cm._clickItemWithCaption("click me")
        }
    }

    test("clicking invisible menu throws exception") {
        val cm: MenuBar = MenuBar().apply {
            item("click me", { _ -> fail("shouldn't be called") }) {
                isVisible = false
            }
        }
        expectThrows(AssertionError::class, "MenuBarRootItem[INVIS, text='click me'] is not visible") {
            cm._clickItemWithCaption("click me")
        }
    }

    test("clicking menu with disabled parent throws exception") {
        val cm: MenuBar = MenuBar().apply {
            item("menu") {
                isEnabled = false
                item("click me", { _ -> fail("shouldn't be called") })
            }
        }
        expectThrows(AssertionError::class, "MenuItem[text='click me'] is not enabled because its parent item is not enabled:") {
            cm._clickItemWithCaption("click me")
        }
    }

    test("clicking menu with invisible parent throws exception") {
        val cm: MenuBar = MenuBar().apply {
            item("menu") {
                isVisible = false
                item("click me", { _ -> fail("shouldn't be called") })
            }
        }
        expectThrows(AssertionError::class, "MenuItem[text='click me'] is not visible because its parent item is not visible:") {
            cm._clickItemWithCaption("click me")
        }
    }

    test("clicking menu on invisible component throws exception") {
        val cm: MenuBar = MenuBar().apply {
            isVisible = false
            item("click me", { _ -> fail("shouldn't be called") })
        }
        expectThrows(AssertionError::class, "Cannot click MenuBarRootItem[text='click me'] since it's attached to MenuBar[INVIS] which is not effectively visible") {
            cm._clickItemWithCaption("click me")
        }
    }
})
