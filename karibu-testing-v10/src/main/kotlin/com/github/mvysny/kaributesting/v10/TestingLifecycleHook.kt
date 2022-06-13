package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.VaadinVersion
import com.github.mvysny.kaributools.component
import com.github.mvysny.kaributools.walk
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.contextmenu.MenuItemBase
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.menubar.MenuBar
import com.vaadin.flow.component.polymertemplate.PolymerTemplate
import com.vaadin.flow.dom.Element
import java.lang.reflect.Method
import kotlin.streams.toList

/**
 * If you need to hook into the testing lifecycle (e.g. you need to wait for any async operations to finish),
 * provide your own custom implementation of this interface, then set it into [testingLifecycleHook].
 * Make sure to call the [TestingLifecycleHook.default] implementation,
 * otherwise Karibu-Testing will not discover the children of basic components.
 *
 * ### Mocking server request end
 *
 * Since Karibu-Testing runs in the same JVM as the server and there is no browser, the boundaries between the client and
 * the server become unclear. When looking into sources of any test method, it's really hard to tell where exactly the server request ends, and
 * where another request starts.
 *
 * You can establish an explicit client boundary in your test, by explicitly calling [MockVaadin.clientRoundtrip]. However, since that
 * would be both laborous and error-prone, the default operation is that Karibu Testing pretends as if there was a client-server
 * roundtrip before every component lookup
 * via the [_get]/[_find]/[_expectNone]/[_expectOne] call. Therefore, [MockVaadin.clientRoundtrip] is called from [awaitBeforeLookup] by default.
 *
 * ### Providing children of a PolymerTemplate/LitTemplate
 *
 * You can override [getAllChildren] to provide children of your particular view extending from PolymerTemplate/LitTemplate.
 * See the Karibu-Testing documentation for more help.
 *
 * ### Delegating properly to the default impl
 * ```
 * class MyLifecycleHook(val delegate: TestingLifecycleHook) : TestingLifecycleHook by delegate {
 *   override fun awaitBeforeLookup() { delegate.awaitBeforeLookup() }
 * }
 * testingLifecycleHook = MyLifecycleHook(TestingLifecycleHook.default)
 * ```
 * @see TestingLifecycleHookVaadin14Default for the default implementation
 */
public interface TestingLifecycleHook {
    /**
     * Invoked before every component lookup. You can e.g. wait for any async operations to finish and for the server to settle down.
     *
     * See [TestingLifecycleHookVaadin14Default.awaitBeforeLookup] for the default implementation.
     */
    public fun awaitBeforeLookup()

    /**
     * Invoked after every component lookup. You can e.g. wait for any async operations to finish and for the server to settle down.
     * Invoked even if the `_get()`/`_find()`/`_expectNone()` function fails.
     *
     * See [TestingLifecycleHookVaadin14Default.awaitAfterLookup] for the default implementation.
     */
    public fun awaitAfterLookup()

    /**
     * Provides all direct children of given component. May include virtual children.
     * Only direct children are considered - don't return children of children.
     *
     * See [TestingLifecycleHookVaadin14Default.getAllChildren] for the default implementation.
     */
    public fun getAllChildren(component: Component): List<Component>

    public companion object {
        /**
         * A default lifecycle hook that works well with all Vaadin versions.
         */
        @JvmStatic
        public val default: TestingLifecycleHook get() {
            var hook: TestingLifecycleHook = TestingLifecycleHookVaadin14Default()
            if (VaadinVersion.get.isAtLeast(23, 1)) {
                hook = TestingLifecycleHookVaadin23_1(hook)
            }
            return hook
        }
    }
}

/**
 * The default implementation of [TestingLifecycleHook] that works for all Vaadin versions.
 * See [TestingLifecycleHookVaadin23_1] for additional bits for Vaadin 23.1.
 */
public open class TestingLifecycleHookVaadin14Default : TestingLifecycleHook {
    /**
     * Calls the [MockVaadin.clientRoundtrip] method. When overriding this method, you should
     * also call [MockVaadin.clientRoundtrip] (or simply call super).
     */
    override fun awaitBeforeLookup() {
        if (UI.getCurrent() != null) {
            MockVaadin.clientRoundtrip()
        }
    }

    /**
     * The function does nothing by default.
     */
    override fun awaitAfterLookup() {
    }

    /**
     * Provides all direct children of given component. Provides workarounds for certain components:
     * * For [Grid.Column] the function will also return cell components nested in all headers and footers for that particular column.
     * * For [MenuItemBase] the function returns all items of a sub-menu.
     */
    override fun getAllChildren(component: Component): List<Component> = when {
        component is Grid<*> -> {
            // don't attach the header/footer components as a child of the Column component:
            // that would make components in merged cells appear more than once.
            // see https://github.com/mvysny/karibu-testing/issues/52
            val headerComponents: List<Component> = component.headerRows
                .flatMap { it.cells.map { it.component } }
                .filterNotNull()
            val footerComponents: List<Component> = component.footerRows
                .flatMap { it.cells.map { it.component } }
                .filterNotNull()
            val editorComponents: List<Component> = component.columns
                .mapNotNull { it.editorComponent }
            val children = component.children.toList()
            (headerComponents + footerComponents + editorComponents + children).distinct()
        }
        component is MenuItemBase<*, *, *> -> {
            // also include component.children: https://github.com/mvysny/karibu-testing/issues/76
            (component.children.toList() + component.subMenu.items).distinct()
        }
        component is MenuBar -> {
            // don't include virtual children since that would make the MenuItems appear two times.
            component.children.toList()
        }
        component is PolymerTemplate<*> -> {
            // don't include virtual children since those will include nested components.
            // however, those components are only they are only "shallow shells" of components constructed
            // server-side - almost none of their properties are transferred to the server-side.
            // Listing those components with null captions and other properties would only be confusing.
            // Therefore, let's leave the virtual children out for now.
            // See https://github.com/mvysny/karibu-testing/tree/master/karibu-testing-v10#polymer-templates--lit-templates
            component.children.toList()
        }
        component.javaClass.name == "com.vaadin.flow.component.grid.ColumnGroup" -> {
            // don't include virtual children since that would include the header/footer components
            // which would clash with Grid.Column later on
            component.children.toList()
        }
        component is Grid.Column<*> -> {
            // don't include virtual children since that would include the header/footer components
            // which would clash with Grid.Column later on
            component.children.toList()
        }
        // Also include virtual children.
        // Issue: https://github.com/mvysny/karibu-testing/issues/85
        else -> (component.children.toList() + component._getVirtualChildren()).distinct()
    }
}

/**
 * Additional bits for Vaadin 23.1.
 */
public open class TestingLifecycleHookVaadin23_1(public val delegate: TestingLifecycleHook) : TestingLifecycleHook by delegate {
    /**
     * Vaadin 23.1 added Dialog.footer and header
     */
    private val dialogHeaderFooter_rootField =
        Class.forName("com.vaadin.flow.component.dialog.Dialog${'$'}DialogHeaderFooter").getDeclaredField("root")
    /**
     * Vaadin 23.1 added Dialog.footer and header
     */
    private val dialog_getHeader =
        Dialog::class.java.getDeclaredMethod("getHeader")
    /**
     * Vaadin 23.1 added Dialog.footer and header
     */
    private val dialog_getFooter =
        Dialog::class.java.getDeclaredMethod("getFooter")

    init {
        dialogHeaderFooter_rootField.isAccessible = true
    }

    /**
     * Vaadin 23.1 added Dialog.footer and header
     */
    private fun getChildrenOfDialogHeaderOrFooter(dialogHeaderFooter: Any): List<Component> {
        val root = dialogHeaderFooter_rootField.get(dialogHeaderFooter) as Element
        return root.children.map { it.component.orElse(null) } .toList().filterNotNull()
    }
    /**
     * Vaadin 23.1 added Dialog.footer and header
     */
    private fun getDialogHeaderChildren(dlg: Dialog): List<Component> {
        val header = dialog_getHeader.invoke(dlg)
        return getChildrenOfDialogHeaderOrFooter(header)
    }
    /**
     * Vaadin 23.1 added Dialog.footer and header
     */
    private fun getDialogFooterChildren(dlg: Dialog): List<Component> {
        val header = dialog_getFooter.invoke(dlg)
        return getChildrenOfDialogHeaderOrFooter(header)
    }

    override fun getAllChildren(component: Component): List<Component> {
        var list = delegate.getAllChildren(component)
        if (component is Dialog) {
            // https://github.com/mvysny/karibu-testing/issues/115
            list = getDialogHeaderChildren(component) + list + getDialogFooterChildren(component)
        }
        return list
    }
}

/**
 * If you need to hook into the testing lifecycle (e.g. you need to wait for any async operations to finish),
 * set your custom implementation here. See [TestingLifecycleHook] for more info on
 * where exactly you can hook into. The best way is to delegate to the [TestingLifecycleHook.default] implementation.
 */
public var testingLifecycleHook: TestingLifecycleHook = TestingLifecycleHook.default

private val _ConfirmDialog_Class: Class<*>? = try {
    Class.forName("com.vaadin.flow.component.confirmdialog.ConfirmDialog")
} catch (e: ClassNotFoundException) { null }
private val _ConfirmDialog_isOpened: Method? =
    _ConfirmDialog_Class?.getMethod("isOpened")

/**
 * Checks whether given [component] is a dialog and needs to be removed from the UI.
 * See [cleanupDialogs] for more info.
 */
private fun isDialogAndNeedsRemoval(component: Component): Boolean {
    if (component is Dialog && !component.isOpened) {
        return true
    }
    // also support ConfirmDialog. But be careful - this is a Pro component and may not be on classpath.
    if (_ConfirmDialog_Class != null && _ConfirmDialog_isOpened != null && _ConfirmDialog_Class.isInstance(component) && !(_ConfirmDialog_isOpened.invoke(component) as Boolean)) {
        return true
    }
    return false
}

/**
 * Flow Server does not close the dialog when [Dialog.close] is called; instead it tells client-side dialog to close,
 * which then fires event back to the server that the dialog was closed, and removes itself from the DOM.
 * Since there's no browser with browserless testing, we need to cleanup closed dialogs manually, hence this method.
 *
 * Also see [com.github.mvysny.kaributesting.v10.mock.MockedUI] for more details
 */
public fun cleanupDialogs() {
    // Starting with Vaadin 23, nested dialogs are also nested within respective
    // modal dialog within the UI. This is probably related to the "server-side
    // modality curtain" feature. Also see https://github.com/mvysny/karibu-testing/issues/102
    UI.getCurrent().walk()
        .filter { isDialogAndNeedsRemoval(it) }
        .forEach { it.element.removeFromParent() }
}
