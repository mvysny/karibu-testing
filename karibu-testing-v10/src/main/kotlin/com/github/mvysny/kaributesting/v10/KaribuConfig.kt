package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation

/**
 * Karibu-Testing configuration.
 */
public object KaribuConfig {
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
    @JvmStatic
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
    @JvmStatic
    public var fakeExtendedClientDetails: Boolean = true

    /**
     * If you need to hook into the testing lifecycle (e.g. you need to wait for any async operations to finish),
     * set your custom implementation here. See [TestingLifecycleHook] for more info on
     * where exactly you can hook into. The best way is to delegate to the [TestingLifecycleHook.default] implementation.
     */
    @JvmStatic
    public var testingLifecycleHook: TestingLifecycleHook = TestingLifecycleHook.default

    /**
     * Called by default from [TestingLifecycleHook.handlePendingJavascriptInvocations].
     * You can register your custom handlers here; they can decide to
     * * ignore the call and do nothing;
     * * Call [PendingJavaScriptInvocation.complete] to complete the invocation successfully
     * * Call [PendingJavaScriptInvocation.completeExceptionally] to complete the invocation with an error.
     * An empty list by default.
     */
    @JvmStatic
    public var pendingJavascriptInvocationHandlers: MutableList<(PendingJavaScriptInvocation) -> Unit> = mutableListOf()

    /**
     * Controls the behavior of [_value]. Since Karibu-Testing 2.4.0, this is set to `true`;
     * to configure Karibu-Testing to emulate previous versions set this to `false`.
     */
    @JvmStatic
    public var defaultIsFromClient: Boolean = true

    /**
     * If `true` (the default), [MockVaadin.setup] also navigates to the root route (`""`).
     */
    @JvmStatic
    public var initDefaultRoute: Boolean = true
}