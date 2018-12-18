package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dialog.Dialog

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
interface TestingLifecycleHook {
    /**
     * Invoked before every component lookup. You can e.g. wait for any async operations to finish and for the server to settle down.
     *
     * The default implementation does two things:
     * * Calls all tasks submitted via [UI.beforeClientResponse]
     * * Calls [cleanupDialogs]
     */
    fun awaitBeforeLookup() {
        if (UI.getCurrent() != null) {
            MockVaadin.clientRoundtrip()
        }
    }

    /**
     * Invoked after every component lookup. You can e.g. wait for any async operations to finish and for the server to settle down.
     * Invoked even if the `_get()`/`_find()`/`_expectNone()` function fails.
     */
    fun awaitAfterLookup() {}

    companion object {
        val noop: TestingLifecycleHook get() = object : TestingLifecycleHook {}
    }
}

/**
 * If you need to hook into the testing lifecycle (e.g. you need to wait for any async operations to finish),
 * set your custom implementation here. See [TestingLifecycleHook] for more info on
 * where exactly you can hook into.
 */
var testingLifecycleHook: TestingLifecycleHook = TestingLifecycleHook.noop

/**
 * Flow Server does not close the dialog when [Dialog.close] is called; instead it tells client-side dialog to close,
 * which then fires event back to the server that the dialog was closed, and removes itself from the DOM.
 * Since there's no browser with browserless testing, we need to cleanup closed dialogs manually, hence this method.
 *
 * Also see [MockedUI] for more details
 */
fun cleanupDialogs() {
    UI.getCurrent().children.forEach {
        if (it is Dialog && !it.isOpened) {
            it.element.removeFromParent()
        }
    }
}
