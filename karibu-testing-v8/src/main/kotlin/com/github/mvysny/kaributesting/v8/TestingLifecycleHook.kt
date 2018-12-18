package com.github.mvysny.kaributesting.v8

import com.vaadin.server.VaadinSession

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
     * The default implementation calls the [MockVaadin.clientRoundtrip] method. When implementing this method, you should
     * also call [MockVaadin.clientRoundtrip] (or simply call super).
     */
    fun awaitBeforeLookup() {
        // this function needs to work properly in unmocked env as well
        if (VaadinSession.getCurrent() != null) {
            MockVaadin.clientRoundtrip()
        }
    }

    /**
     * Invoked after every component lookup. You can e.g. wait for any async operations to finish and for the server to settle down.
     * Invoked even if the `_get()`/`_find()`/`_expectNone()` function fails.
     *
     * The default implementation does nothing.
     */
    fun awaitAfterLookup() {}

    companion object {
        val default: TestingLifecycleHook get() = object : TestingLifecycleHook {}
    }
}

/**
 * If you need to hook into the testing lifecycle (e.g. you need to wait for any async operations to finish),
 * set your custom implementation here. See [TestingLifecycleHook] for more info on
 * where exactly you can hook into. Defaults to [TestingLifecycleHook.default].
 */
var testingLifecycleHook: TestingLifecycleHook = TestingLifecycleHook.default
