package com.github.mvysny.kaributesting.v10

import com.vaadin.flow.component.internal.PendingJavaScriptInvocation
import com.vaadin.flow.component.page.Page

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

    /**
     * Controls *when*, during an F5 [Page.reload], Karibu simulates the browser unload beacon
     * (the [Beacon API](https://developer.mozilla.org/en-US/docs/Web/API/Beacon_API)'s
     * `navigator.sendBeacon`) that closes the old UI, relative to the creation of the new UI.
     *
     * Only affects **non-`@PreserveOnRefresh`** targets: real Vaadin Flow ignores the beacon for a
     * [com.vaadin.flow.router.PreserveOnRefresh] view (`ServerRpcHandler.handleUnloadBeaconRequest`
     * skips the close), and instead closes the old UI from the new UI's navigation while teleporting
     * any open dialogs/notifications onto the new UI — that behavior is fixed and this setting does
     * not change it.
     *
     * Defaults to [UnloadBeaconTiming.EAGER], which matches both the common production ordering and
     * the pre-2.7.1 Karibu behavior for non-`@PreserveOnRefresh` reloads.
     *
     * See `ideas/beacon-reload-timing.md` for the full analysis.
     */
    @JvmStatic
    public var unloadBeaconTiming: UnloadBeaconTiming = UnloadBeaconTiming.EAGER
}

/**
 * Controls when the browser unload beacon (`navigator.sendBeacon`) is simulated during an F5
 * [Page.reload], relative to the creation of the new UI. Only affects non-`@PreserveOnRefresh`
 * targets — see [KaribuConfig.unloadBeaconTiming].
 */
public enum class UnloadBeaconTiming {
    /**
     * The beacon arrives *before* the new UI is created: the old UI is closed, detached and removed
     * from the session first, then the new UI is created. This is the common production ordering
     * (the beacon is fired on page unload, before the reloaded page boots and creates the new UI),
     * and it matches the pre-2.7.1 Karibu behavior. The default.
     */
    EAGER,

    /**
     * The beacon arrives *after* the new UI is created: the old and new UI are both briefly live,
     * then the old UI is closed, detached and removed. Models a beacon that lands late (e.g. after
     * the reloaded page has already initialized its UI).
     */
    LATE,

    /**
     * The beacon is never delivered ("beacon lost"): the old UI is left alive alongside the new one
     * and is not closed. Real Flow would eventually reap the lingering UI via the heartbeat timeout;
     * Karibu does not model that clock, so under this setting the old UI lingers until you call
     * [MockVaadin.reapInactiveUIs], which simulates that reap (its outcome, not its timing).
     */
    NEVER,
}