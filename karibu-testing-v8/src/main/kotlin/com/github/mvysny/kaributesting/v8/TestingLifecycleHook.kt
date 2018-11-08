package com.github.mvysny.kaributesting.v8

interface TestingLifecycleHook {
    /**
     * Invoked before every component lookup. You can e.g. wait for any async operations to finish and for the server to settle down.
     */
    fun awaitBeforeLookup() {}

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
