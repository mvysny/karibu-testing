package com.github.karibu.testing.v10

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dialog.Dialog

interface TestingLifecycleHook {
    /**
     * Invoked before every component lookup. You can e.g. wait for any async operations to finish and for the server to settle down.
     *
     * The default implementation calls [cleanupDialogs].
     */
    fun awaitBeforeLookup() {
        if (UI.getCurrent() != null) {
            cleanupDialogs()
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
 * Flow Server does not close the dialog when close() is called; instead it tells client-side dialog to close,
 * which then fires event back to the server that the dialog was closed, and removes itself from the DOM.
 * Since there's no browser with browserless testing, we need to cleanup closed dialogs manually, hence this method.
 *
 * Also see [MockedUI] for more details
 */
fun cleanupDialogs() {
    UI.getCurrent().children.forEach {
        if (it is Dialog && !it.isOpened) it.element.removeFromParent()
    }
}
