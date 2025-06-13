package com.github.mvysny.kaributesting.v10

import com.github.mvysny.kaributools.label
import com.github.mvysny.kaributools.walk
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.contextmenu.MenuItemBase
import com.vaadin.flow.component.contextmenu.SubMenuBase
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.internal.PendingJavaScriptInvocation
import com.vaadin.flow.component.littemplate.LitTemplate
import com.vaadin.flow.component.menubar.MenuBar
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem

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
 *
 * It's **super-important** to delegate to the original hook, so that [MockVaadin.clientRoundtrip] is called. Otherwise, you'll\
 * experience strange behavior like the Dialogs not being connected to the UI upon opening.
 *
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
     * It's **super-important** to delegate to the original hook, so that [MockVaadin.clientRoundtrip] is called. Otherwise, you'll\
     * experience strange behavior like the Dialogs not being connected to the UI upon opening.
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

    /**
     * Returns the label of the component. According to the [official recommendation](https://github.com/vaadin/flow-components/issues/5129), only Vaadin
     * fields should implement [com.vaadin.flow.component.HasLabel]. However, for testing purposes
     * it's convenient to be able to look up also Tab and SideNavItem by their labels (and possibly other components as well,
     * including your own custom components). That's exactly what this function is for - to retrieve the
     * label from components other than [com.vaadin.flow.component.HasLabel].
     *
     * The default implementation only covers the Vaadin built-in components.
     */
    public fun getLabel(component: Component): String?

    /**
     * Invoked by [MockVaadin.clientRoundtrip] to handle any pending javascript invocations.
     *
     * The default implementation calls all [pendingJavascriptInvocationHandlers].
     */
    public fun handlePendingJavascriptInvocations(invocations: List<PendingJavaScriptInvocation>) {
        invocations.forEach { invocation ->
            pendingJavascriptInvocationHandlers.forEach { it.invoke(invocation) }
        }
    }

    public companion object {
        /**
         * A default lifecycle hook that works well with all Vaadin versions.
         */
        @JvmStatic
        public val default: TestingLifecycleHook get() = TestingLifecycleHookVaadin14Default()
    }
}

/**
 * The default implementation of [TestingLifecycleHook] that works for all Vaadin 24+ versions.
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
            val items: List<Component> = (component.subMenu as SubMenuBase<*, *, *>).items
            // also include component.children: https://github.com/mvysny/karibu-testing/issues/76
            val children = component.children.toList()
            // also include MenuManager's children: https://github.com/mvysny/karibu-testing/issues/163
            val menuManagerChildren = component.subMenu._menuManager.children.toList()
            (children + items + menuManagerChildren).distinct()
        }
        component is MenuBar -> {
            // don't include virtual children since that would make the MenuItems appear two times.
            component.children.toList()
        }
        component.isTemplate && !includeVirtualChildrenInTemplates -> {
            // don't include virtual children; see [includeVirtualChildrenInTemplates] for more details.
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
        component is Composite<*> -> {
            // The Composite class overrides getChildren() to return a stream with the wrapped component, but also getElement() returning the Element of the wrapped component.
            // The latter causes the virtual child to be fetched as Composite direct child, thus duplicating any virtual children the child component might have.
            component.children.toList()
        }
        // Also include virtual children.
        // Issue: https://github.com/mvysny/karibu-testing/issues/85
        else -> (component.children.toList() + component._getVirtualChildren()).distinct()
    }

    override fun getLabel(component: Component): String? = when (component) {
        is SideNav -> component.label
        is SideNavItem -> component.label
        else -> component.label
    }
}

internal val Component.isTemplate: Boolean get() = this is LitTemplate

/**
 * [PolymerTemplate]s and LitTemplates are a bit tricky.
 * The purpose of PolymerTemplates is to move as much code as possible to the client-side,
 * while Karibu is designed to test server-side code only. The child components
 * are either not accessible from the server-side altogether,
 * or they are only "shallow shells" of components constructed server-side -
 * almost none of their properties are transferred to the server-side.
 *
 * Also see [Polymer Templates / Lit Templates](https://github.com/mvysny/karibu-testing/tree/master/karibu-testing-v10#polymer-templates--lit-templates)
 * for more info.
 *
 * Theese child components are still available on server-side and attached to the Template as virtual children, therefore
 * it is possible to obtain them from the server-side. If you understand the risks
 * and shortcomings of this, set this property to `true` to include virtual children in
 * Karibu-recognized tree of components.
 */
public var includeVirtualChildrenInTemplates: Boolean = false

/**
 * By default, Karibu fakes [MockPage.retrieveExtendedClientDetails].
 * The [createExtendedClientDetails] function is used to construct fake ECD (ExtendedClientDetails).
 *
 * To set custom ECD, provide a custom UI factory in [MockVaadin.setup] which
 * creates a UI and populates it with ECD.
 *
 * Turning this off will cause `@PreserveOnRefresh` not to work anymore, see [Issue #118](https://github.com/mvysny/karibu-testing/issues/118)
 * for more details.
 *
 * Expert setting: you most probably don't need to touch this!
 */
public var fakeExtendedClientDetails: Boolean = true

/**
 * If you need to hook into the testing lifecycle (e.g. you need to wait for any async operations to finish),
 * set your custom implementation here. See [TestingLifecycleHook] for more info on
 * where exactly you can hook into. The best way is to delegate to the [TestingLifecycleHook.default] implementation.
 */
public var testingLifecycleHook: TestingLifecycleHook = TestingLifecycleHook.default

/**
 * Called by default from [TestingLifecycleHook.handlePendingJavascriptInvocations].
 * You can register your custom handlers here; they can decide to
 * * ignore the call and do nothing;
 * * Call [PendingJavaScriptInvocation.complete] to complete the invocation successfully
 * * Call [PendingJavaScriptInvocation.completeExceptionally] to complete the invocation with an error.
 * An empty list by default.
 */
public var pendingJavascriptInvocationHandlers: MutableList<(PendingJavaScriptInvocation) -> Unit> = mutableListOf()

/**
 * Controls the behavior of [_value]. Since Karibu-Testing 2.4.0, this is set to `true`;
 * to configure Karibu-Testing to emulate previous versions set this to `false`.
 */
public var defaultIsFromClient: Boolean = true

/**
 * Checks whether given [component] is a dialog and needs to be removed from the UI.
 * See [cleanupDialogs] for more info.
 */
private fun isDialogAndNeedsRemoval(component: Component): Boolean {
    if (component is Dialog && !component.isOpened) {
        return true
    }
    // also support ConfirmDialog. Since Vaadin 24 it's no longer a Pro component.
    if (component is ConfirmDialog && !component.isOpened) {
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
