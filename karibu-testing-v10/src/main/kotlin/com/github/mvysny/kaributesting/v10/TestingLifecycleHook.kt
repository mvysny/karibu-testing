package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.contextmenu.MenuItemBase
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.polymertemplate.PolymerTemplate
import java.lang.reflect.Method
import kotlin.streams.toList

/**
 * If you need to hook into the testing lifecycle (e.g. you need to wait for any async operations to finish),
 * provide your own custom implementation of this interface, then set it into [testingLifecycleHook].
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
 */
public interface TestingLifecycleHook {
    /**
     * Invoked before every component lookup. You can e.g. wait for any async operations to finish and for the server to settle down.
     *
     * The default implementation calls the [MockVaadin.clientRoundtrip] method. When implementing this method, you should
     * also call [MockVaadin.clientRoundtrip] (or simply call super).
     */
    public fun awaitBeforeLookup() {
        if (UI.getCurrent() != null) {
            MockVaadin.clientRoundtrip()
        }
    }

    /**
     * Invoked after every component lookup. You can e.g. wait for any async operations to finish and for the server to settle down.
     * Invoked even if the `_get()`/`_find()`/`_expectNone()` function fails.
     */
    public fun awaitAfterLookup() {}

    /**
     * Provides all children of given component. Provides workarounds for certain components:
     * * For [Grid.Column] the function will also return cell components nested in all headers and footers for that particular column.
     * * For [MenuItemBase] the function returns all items of a sub-menu.
     */
    public fun getAllChildren(component: Component): List<Component> = when(component) {
        is Grid<*> -> {
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
            headerComponents + footerComponents + editorComponents + component.children.toList()
        }
        is MenuItemBase<*, *, *> -> {
            // also include component.children: https://github.com/mvysny/karibu-testing/issues/76
            (component.children.toList() + component.subMenu.items).distinct()
        }
        is PolymerTemplate<*> -> {
            // Issue: https://github.com/mvysny/karibu-testing/issues/85
            // don't include virtual children since those will include nested components.
            // however, those components are only they are only "shallow shells" of components constructed
            // server-side - almost none of their properties are transferred to the server-side.
            // Listing those components with null captions and other properties would only be confusing.
            // Therefore, let's leave the virtual children out for now.
            // See https://github.com/mvysny/karibu-testing/tree/master/karibu-testing-v10#polymer-templates--lit-templates
            component.children.toList()
        }
        // Also include virtual children.
        // Issue: https://github.com/mvysny/karibu-testing/issues/85
        else -> (component.children.toList() + component._getVirtualChildren()).distinct()
    }

    public companion object {
        /**
         * A default lifecycle hook that simply runs default implementations of the hook functions.
         */
        public val default: TestingLifecycleHook get() = object : TestingLifecycleHook {}
    }
}

/**
 * If you need to hook into the testing lifecycle (e.g. you need to wait for any async operations to finish),
 * set your custom implementation here. See [TestingLifecycleHook] for more info on
 * where exactly you can hook into.
 */
public var testingLifecycleHook: TestingLifecycleHook = TestingLifecycleHook.default

/**
 * Flow Server does not close the dialog when [Dialog.close] is called; instead it tells client-side dialog to close,
 * which then fires event back to the server that the dialog was closed, and removes itself from the DOM.
 * Since there's no browser with browserless testing, we need to cleanup closed dialogs manually, hence this method.
 *
 * Also see [MockedUI] for more details
 */
public fun cleanupDialogs() {
    UI.getCurrent().children.forEach {
        if (it is Dialog && !it.isOpened) {
            it.element.removeFromParent()
        }
    }

    // also clean up ConfirmDialog. But careful - this is a Pro component and may not be on classpath.
    val dlgClass: Class<*>? = try {
        Class.forName("com.vaadin.flow.component.confirmdialog.ConfirmDialog")
    } catch (e: ClassNotFoundException) { null }
    if (dlgClass != null) {
        val isOpenedMethod: Method = dlgClass.getMethod("isOpened")
        UI.getCurrent().children.forEach {
            if (dlgClass.isInstance(it) && !(isOpenedMethod.invoke(it) as Boolean)) {
                it.element.removeFromParent()
            }
        }
    }
}
