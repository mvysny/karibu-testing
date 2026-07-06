package com.github.mvysny.kaributesting.v10

import com.github.mvysny.fakeservlet.FakeHttpSession
import com.github.mvysny.fakeservlet.FakeRequest
import com.github.mvysny.fakeservlet.FakeResponse
import com.github.mvysny.fakeservlet.FakeServletConfig
import com.github.mvysny.kaributesting.mockhttp.*
import com.github.mvysny.kaributesting.v10.mock.*
import com.github.mvysny.kaributools.VaadinVersion
import com.vaadin.flow.component.ComponentUtil
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.page.ExtendedClientDetails
import com.vaadin.flow.component.page.Page
import com.vaadin.flow.internal.CurrentInstance
import com.vaadin.flow.internal.StateTree
import com.vaadin.flow.router.Location
import com.vaadin.flow.router.NavigationTrigger
import com.vaadin.flow.router.PreserveOnRefresh
import com.vaadin.flow.server.*
import com.vaadin.flow.shared.communication.PushMode
import java.lang.reflect.Field
import java.util.concurrent.ExecutionException
import java.util.concurrent.locks.ReentrantLock
import jakarta.servlet.ServletContext
import kotlin.test.expect

public object MockVaadin {
    // prevent GC on Vaadin Session and Vaadin UI as they are only soft-referenced from the Vaadin itself.
    // use ThreadLocals so that multiple threads may initialize fresh Vaadin instances at the same time.
    private val strongRefSession = ThreadLocal<VaadinSession>()
    private val strongRefUI = ThreadLocal<UI>()
    private val strongRefReq = ThreadLocal<VaadinRequest>()
    private val strongRefRes = ThreadLocal<VaadinResponse>()

    /**
     * The UI factory passed to [setup]; remembered so [MockBrowser] can spawn additional tabs
     * (UIs) into the current session using the same factory.
     */
    private val strongRefUiFactory = ThreadLocal<() -> UI>()

    /**
     * When closing UI via [MockVaadin.closeCurrentUI], the UI location is remembered here.
     *
     * The reason is: when reloading the page via [Page.reload], the current UI is closed
     * and a new one is opened; the new one needs to preserve the location of the old one.
     *
     * See [MockPage.reload] for more details.
     */
    private val lastUILocation = ThreadLocal<Location>()

    /**
     * Marks a UI that Karibu left alive because its unload beacon was lost
     * ([UnloadBeaconTiming.NEVER] on an F5 [Page.reload]). [reapInactiveUIs] reaps exactly these,
     * emulating the outcome of Flow's heartbeat / idle-UI cleanup.
     */
    private const val UNLOAD_BEACON_LOST_KEY: String =
        "com.github.mvysny.kaributesting.v10.unloadBeaconLost"

    /**
     * Mocks Vaadin for the current test method:
     * ```
     * MockVaadin.setup(Routes().autoDiscoverViews("com.myapp"))
     * ```
     *
     * The UI factory *must* provide a new, fresh instance of the UI, so that the
     * tests start from a pre-known state. If you're using Spring and you're getting UI
     * from the injector, you must reconfigure Spring to use prototype scope,
     * otherwise an old UI from the UI scope or Session Scope will be provided.
     *
     * Sometimes you wish to provide a specific [VaadinServletService],
     * e.g. to override
     * [VaadinServletService.loadInstantiators] and provide your own way of instantiating Views, e.g. via Spring or Guice.
     * Please do that by extending [MockVaadinServlet] and overriding [MockVaadinServlet.createServletService]
     * `createServletService(DeploymentConfiguration)`.
     * Please consult [MockService] on what methods you must override in your custom service.
     * Alternatively, see `MockSpringServlet` on how to extend your custom servlet and
     * provide all necessary mocking code.
     * @param routes all classes annotated with [com.vaadin.flow.router.Route]; use [Routes.autoDiscoverViews] to auto-discover all such classes.
     * @param uiFactory produces [UI] instances and sets them as current, by default simply instantiates [MockedUI] class.
     */
    @JvmStatic
    @JvmOverloads
    public fun setup(routes: Routes = Routes(),
              uiFactory: () -> UI = @JvmSerializableLambda { MockedUI() }) {
        // init servlet
        val servlet = MockVaadinServlet(routes, uiFactory)
        setup(uiFactory, servlet)
    }

    /**
     * Use this method when you need to provide a completely custom servlet (e.g. `SpringServlet`). Do not forget to create a specialized service
     * which works in mocked environment. See below for details on how to do this.
     *
     * The UI factory *must* provide a new, fresh instance of the UI, so that the
     * tests start from a pre-known state. If you're using Spring and you're getting UI
     * from the injector, you must reconfigure Spring to use prototype scope,
     * otherwise an old UI from the UI scope or Session Scope will be provided.
     * @param uiFactory produces [UI] instances and sets them as current, by default
     * simply instantiates [MockedUI] class.
     * @param servlet allows you to provide your own implementation of [VaadinServlet].
     * You MUST override [VaadinServlet.createServletService]
     * and construct a custom service which overrides important methods.
     * Please consult [com.github.mvysny.kaributesting.v10.mock.MockService]
     * on what methods you must override in your custom service.
     */
    @JvmStatic
    public fun setup(uiFactory: () -> UI = @JvmSerializableLambda { MockedUI() }, servlet: VaadinServlet) {
        check(VaadinVersion.get.isAtLeast(25)) {
            "Karibu-Testing 2.6.x only works with Vaadin 25+ but you're using ${VaadinVersion.get}"
        }
        check(!VaadinMeta.isCompatibilityMode)

        // disable the Page.reload() detection if tearDown() was not called.
       lastUILocation.remove()

        // initialize servlet if necessary
        if (!servlet.isInitialized) {
            val ctx: ServletContext = MockVaadinHelper.createMockContext()
            servlet.init(FakeServletConfig(ctx))
        }
        val service: VaadinServletService = checkNotNull(servlet.service)
        check(service.router != null) { "$servlet failed to call VaadinServletService.init() in createServletService()" }
        VaadinService.setCurrent(service)

        // init Vaadin Session
        createSession(servlet.servletContext, uiFactory)
    }

    /**
     * Properly closes the current UI and fire the detach event on it.
     * Does nothing if there is no current UI.
     */
    public fun closeCurrentUI(fireUIDetach: Boolean) {
        val ui: UI = UI.getCurrent() ?: return
        lastUILocation.set(ui.internals.activeViewLocation)
        if (ui.isClosing && ui.internals.session != null) {
            ui._close()
        }
        if (fireUIDetach) {
            ComponentUtil.onComponentDetach(ui)
        }
        UI.setCurrent(null)
        strongRefUI.remove()
    }

    /**
     * Simulates a browser F5 reload of the current UI, as closely to real Vaadin Flow
     * as a server-side-only test double allows. See
     * [issue 207](https://github.com/mvysny/karibu-testing/issues/207) and
     * `ideas/beacon-reload-timing.md` for the full analysis.
     *
     * **[com.vaadin.flow.router.PreserveOnRefresh] target.** Real Flow keeps the *old* UI alive
     * while the *new* UI navigates, so that
     * [com.vaadin.flow.router.internal.AbstractNavigationStateRenderer] `.disconnectElements()` can:
     * 1. mark the old UI with Flow's internal `ReplacedViaPreserveOnRefresh` sentinel (via
     *    `Element.removeFromTree()`),
     * 2. **teleport UI-level overlays (dialogs/notifications) off the old UI onto the new UI**
     *    (via [com.vaadin.flow.component.internal.UIInternals.moveElementsFrom]),
     * 3. close the old UI.
     *
     * Because Karibu drives Flow's *real* navigation pipeline, we get all of the above (the
     * sentinel, the overlay teleport, the overlays-before-route child ordering and `oldUI.close()`)
     * for free - **as long as the old UI is still alive and still owns the preserved route root
     * when the new UI navigates.** So for this case the job is: create the new UI first, then discard
     * the old one. The browser unload beacon is ignored by Flow here
     * (`ServerRpcHandler.handleUnloadBeaconRequest` skips it), so [KaribuConfig.unloadBeaconTiming]
     * has no effect.
     *
     * **Non-[com.vaadin.flow.router.PreserveOnRefresh] target.** Flow does not go through
     * `disconnectElements()`; the browser's unload beacon closes the old UI instead. Its timing
     * relative to the creation of the new UI is best-effort in a real browser, so it is configurable
     * via [KaribuConfig.unloadBeaconTiming] ([UnloadBeaconTiming.EAGER] by default). Either way the
     * old UI ends up closed (via [UI.close]), detached and removed from the session - matching Flow's
     * `ui.close()` + end-of-request `removeClosedUIs()`.
     */
    internal fun reloadCurrentUI(uiFactory: () -> UI, session: VaadinSession, newWindowName: String? = null) {
        val oldUI: UI = checkNotNull(UI.getCurrent()) { "No current UI to reload" }
        // The new UI keeps the reloaded tab's window.name unless the caller overrides it. A changed
        // window.name models browsers/navigations that don't preserve it across reload (e.g. Safari
        // with dev tools closed, or typed-URL navigation) - see MockBrowser.reload.
        val windowName: String = newWindowName ?: oldUI.windowName
        // remember the current location so the new UI navigates to the same place.
        lastUILocation.set(oldUI.internals.activeViewLocation)

        // createUI() picks a fresh uiId that steps past every tab currently in the session, so
        // addUI() never evicts a still-live UI - neither the reloaded-but-not-yet-discarded old UI
        // nor any other open browser tab.
        if (isPreserveOnRefreshTarget(oldUI)) {
            // Keep the old UI alive & registered so the new UI's navigation can teleport overlays
            // off it and close it (see kdoc). Then discard the old UI.
            createUI(uiFactory, session, windowName = windowName)
            discardOldUI(oldUI)
        } else when (KaribuConfig.unloadBeaconTiming) {
            UnloadBeaconTiming.EAGER -> {
                // Beacon before the new UI exists: close+detach+remove the old UI, then create the new.
                discardOldUI(oldUI)
                createUI(uiFactory, session, windowName = windowName)
            }
            UnloadBeaconTiming.LATE -> {
                // Beacon after the new UI is created: both are briefly live, then the old UI is
                // closed+detached+removed.
                createUI(uiFactory, session, windowName = windowName)
                discardOldUI(oldUI)
            }
            UnloadBeaconTiming.NEVER -> {
                // Beacon lost: leave the old UI alive alongside the new one. Flag it so
                // reapInactiveUIs() can later close it the way Flow's heartbeat/idle-UI cleanup would.
                ComponentUtil.setData(oldUI, UNLOAD_BEACON_LOST_KEY, true)
                createUI(uiFactory, session, windowName = windowName)
            }
        }
    }

    /**
     * Closes, detaches and removes [oldUI] from its session, mirroring Vaadin's end-of-request
     * cleanup (`removeClosedUIs()`) after a UI is closed. [UI._close] removes the UI from the session
     * and fires its detach listeners; it asserts that the UI being removed is the current one, so we
     * briefly restore [oldUI] as current and then restore whatever was current before (unless that
     * was [oldUI] itself, which is now gone).
     */
    private fun discardOldUI(oldUI: UI) {
        val previous: UI? = UI.getCurrent()
        UI.setCurrent(oldUI)
        try {
            oldUI._close()
        } finally {
            UI.setCurrent(if (previous === oldUI) null else previous)
        }
    }

    /**
     * Reaps UIs that were abandoned by a **lost unload beacon** — i.e. left alive by
     * [UnloadBeaconTiming.NEVER] on an F5 [Page.reload] (the "browser tab was closed but the beacon
     * was lost" case). Each such UI is closed ([UI.close]), detached (its detach listeners fire) and
     * removed from the session, exactly the way [discardOldUI] handles the eager/late beacon.
     *
     * This emulates the **outcome** of Flow's heartbeat / idle-UI cleanup
     * (`VaadinService.closeInactiveUIs`, which reaps a UI once it has missed ~3 heartbeat intervals)
     * for the one lingering-UI scenario a browserless test can produce. It deliberately does **not**
     * model the timing: the reap happens immediately when you call this, not after N intervals, and
     * Karibu tracks no heartbeats. The current UI is never reaped — in a test it is the live UI under
     * test. A no-op if no beacon was lost.
     */
    @JvmStatic
    public fun reapInactiveUIs() {
        val session: VaadinSession = checkNotNull(VaadinSession.getCurrent()) {
            "No VaadinSession - was MockVaadin.setup() called?"
        }
        val current: UI? = UI.getCurrent()
        // filter{} snapshots into a fresh list, so discardOldUI() -> session.removeUI() below won't
        // concurrently modify the collection we're iterating.
        session.uIs
            .filter { it !== current && ComponentUtil.getData(it, UNLOAD_BEACON_LOST_KEY) == true }
            .forEach { discardOldUI(it) }
    }

    /**
     * Mirrors `ServerRpcHandler.isPreserveOnRefreshTarget`: whether the UI currently shows a
     * [com.vaadin.flow.router.PreserveOnRefresh] route target.
     */
    private fun isPreserveOnRefreshTarget(ui: UI): Boolean =
        ui.internals.activeRouterTargetsChain.any {
            it.javaClass.isAnnotationPresent(PreserveOnRefresh::class.java)
        }

    /**
     * Cleans up and removes the Vaadin UI and Vaadin Session. You can call this function in `afterEach{}` block,
     * to clean up after the test. This comes handy when you want to be extra-sure that the next test won't accidentally reuse old UI,
     * should you forget to call [setup] properly.
     *
     * You don't have to call this function though; [setup] will overwrite any current UI/Session instances with a fresh ones.
     *
     * Any exceptions thrown by listeners such as UI detach listener, or session/service destroy listeners, or scheduled calls
     * to [UI.access] will be propagated and thrown by this function.
     */
    @JvmStatic
    public fun tearDown() {
        try {
            clearVaadinInstances(false)
        } finally {
            lastUILocation.remove()
        }
        val service: VaadinService? = VaadinService.getCurrent()
        if (service != null) {
            service.fireServiceDestroyListeners(ServiceDestroyEvent(service))
            VaadinService.setCurrent(null)
        }
    }

    private fun clearVaadinInstances(fireUIDetach: Boolean) {
        try {
            discardBackgroundUIs()
            closeCurrentUI(fireUIDetach)
            closeCurrentSession()
        } finally {
            CurrentInstance.set(VaadinRequest::class.java, null)
            CurrentInstance.set(VaadinResponse::class.java, null)
            strongRefReq.remove()
            strongRefRes.remove()
            strongRefUiFactory.remove()
        }
    }

    /**
     * Closes, detaches and removes every UI (browser tab) in the current session **except** the
     * current one, which [closeCurrentUI]/[closeCurrentSession] handle. Ensures teardown nukes all
     * tabs opened via [MockBrowser.newTab] and any UIs left lingering by a lost unload beacon, not
     * just the focused tab. A no-op if there is no session or no extra tabs.
     */
    private fun discardBackgroundUIs() {
        val session: VaadinSession = VaadinSession.getCurrent() ?: return
        val current: UI? = UI.getCurrent()
        // filter{} snapshots into a fresh list so discardOldUI() -> session.removeUI() below does not
        // concurrently modify the collection we're iterating.
        session.uIs
            .filter { it !== current }
            .forEach { discardOldUI(it) }
    }

    /**
     * Closes current session if [VaadinSession.getCurrent] is not null.
     */
    private fun closeCurrentSession() {
        val session: VaadinSession? = VaadinSession.getCurrent()
        strongRefSession.remove()
        if (session != null) {
            val service: VaadinService = VaadinService.getCurrent()
            service.fireSessionDestroy(session)
            VaadinSession.setCurrent(null)
            // service destroys session via session.access(); we need to run that action now.
            currentlyClosingSession.set(true)
            try {
                runUIQueue(session = session)
            } finally {
                currentlyClosingSession.set(false)
            }
        }
    }

    private val currentlyClosingSession: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }

    /**
     * Change & call [setup] to set a different browser.
     *
     * The default is Firefox 94 on Ubuntu Linux.
     */
    @Deprecated(
        "Browser identity moved to MockBrowser; use MockBrowser.userAgent instead.",
        ReplaceWith("MockBrowser.userAgent", "com.github.mvysny.kaributesting.v10.MockBrowser")
    )
    public var userAgent: String
        get() = MockBrowser.userAgent
        set(value) { MockBrowser.userAgent = value }

    /**
     * Creates [MockRequest]; override if you need to return a class that extends [MockRequest]
     * and modifies its behavior.
     */
    @JvmStatic
    public var mockRequestFactory: (FakeHttpSession) -> FakeRequest = { FakeRequest(it) }

    internal fun createVaadinRequest(httpSession: FakeHttpSession = currentSession.fake): VaadinServletRequest {
        val mockRequest = mockRequestFactory(httpSession)
        // so that session.browser.updateRequestDetails() also creates browserDetails
        mockRequest.headers["User-Agent"] = listOf(MockBrowser.userAgent)
        return VaadinServletRequest(mockRequest, currentService as VaadinServletService)
    }

    internal fun createVaadinResponse() =
        VaadinServletResponse(FakeResponse(), currentService as VaadinServletService)

    private fun createSession(ctx: ServletContext, uiFactory: () -> UI) {
        val service: VaadinServletService = checkNotNull(VaadinService.getCurrent()) as VaadinServletService
        val httpSession: FakeHttpSession = FakeHttpSession.create(ctx)

        // init Vaadin Request
        val request = createVaadinRequest(httpSession)
        strongRefReq.set(request)
        CurrentInstance.set(VaadinRequest::class.java, request)

        // init Session.
        // Use the underlying Service to create the Vaadin Session; however
        // you MUST mock certain things in order for Karibu to work.
        // See MockSession for more details. By default the service is a MockService
        // which creates MockSession.
        val session: VaadinSession = service._createVaadinSession(VaadinRequest.getCurrent())
        httpSession.setAttribute(service.serviceName + ".lock", ReentrantLock().apply { lock() })
        httpSession.setAttribute(VaadinSession::class.java.name + "." + service.serviceName, session)
        session.refreshTransients(WrappedHttpSession(httpSession), service)
        check(session.lockInstance != null) { "$session created from $service has null lock. See the MockSession class on how to mock locks properly" }
        check((session.lockInstance as ReentrantLock).isLocked) { "$session created from $service: lock must be locked!" }
        // make sure session has correct VaadinConfiguration
        // In Vaadin 25, VaadinSession retrieves the configuration from VaadinService
        check(session.configuration === service.deploymentConfiguration) { "Expected ${service.deploymentConfiguration} but got ${session.configuration}" }

        VaadinSession.setCurrent(session)
        strongRefSession.set(session)
        session.browser = WebBrowser(request)
        checkNotNull(session.browser.browserApplication) { "The WebBrowser has not been mocked properly" }

        // init Vaadin Response
        val response = createVaadinResponse()
        strongRefRes.set(response)
        CurrentInstance.set(VaadinResponse::class.java, response)

        // fire session init listeners
        service.fireSessionInitListeners(SessionInitEvent(service, session, request))

        // remember the factory so MockBrowser.newTab()/reload() can spawn more tabs into this session.
        strongRefUiFactory.set(uiFactory)

        // create the first UI/tab, seeded with KaribuConfig.windowName.
        createUI(uiFactory, session)
    }

    /**
     * The [setup]-supplied UI factory for the current session, used by [MockBrowser] to spawn
     * additional tabs. Throws if [setup] was not called.
     */
    internal val currentUiFactory: () -> UI
        get() = checkNotNull(strongRefUiFactory.get()) {
            "No UI factory - was MockVaadin.setup() called?"
        }

    /**
     * Makes [ui] the current UI (and keeps a strong ref to it). Used by [MockBrowser.switchTo] to
     * move focus between tabs of the same session.
     */
    internal fun focusUI(ui: UI) {
        UI.setCurrent(ui)
        strongRefUI.set(ui)
    }

    /**
     * Closes, detaches and removes [ui] from its session - see [discardOldUI]. Exposed for
     * [MockBrowser.closeTab] (beacon delivered).
     */
    internal fun discardUI(ui: UI) {
        discardOldUI(ui)
    }

    /**
     * Marks [ui] as having lost its unload beacon, so a later [reapInactiveUIs] closes it. Used by
     * [MockBrowser.closeTab] with `beaconLost = true`, mirroring the [UnloadBeaconTiming.NEVER]
     * reload path.
     */
    internal fun markUnloadBeaconLost(ui: UI) {
        ComponentUtil.setData(ui, UNLOAD_BEACON_LOST_KEY, true)
    }

    /**
     * Opens a new UI (browser tab) in the current session, alongside the existing tabs: a fresh
     * `uiId` (so [VaadinSession.addUI] doesn't evict the others), the given [windowName], navigated to
     * [path] (a *fresh* navigation, unlike a location-preserving reload). The new UI becomes current.
     * Backs [MockBrowser.newTab].
     */
    internal fun openNewTab(windowName: String, path: String): UI {
        val session: VaadinSession = checkNotNull(VaadinSession.getCurrent()) {
            "No VaadinSession - was MockVaadin.setup() called?"
        }
        // createUI() assigns a fresh uiId that doesn't evict the existing tabs.
        lastUILocation.set(Location(path))
        createUI(currentUiFactory, session, windowName = windowName)
        return checkNotNull(UI.getCurrent())
    }

    internal fun createUI(
        uiFactory: () -> UI,
        session: VaadinSession,
        // uiId doubles as the key in VaadinSession.uIs, so it must not clash with any UI already in
        // the session, else session.addUI() would evict that (still-live) UI. Default to the next free
        // id: for the first UI of a fresh session that is 1; for a reload/new tab it steps past every
        // tab currently open (the reloaded-but-not-yet-discarded old UI, plus any other browser tabs).
        uiId: Int = (session.uIs.maxOfOrNull { it.uiId } ?: 0) + 1,
        windowName: String = KaribuConfig.windowName
    ) {
        val request: VaadinRequest = checkNotNull(VaadinRequest.getCurrent())
        val ui: UI = uiFactory()
        require(ui.session == null) {
            "uiFactory produced UI $ui which is already attached to a Session, " +
                    "yet we expect the UI to be a fresh new instance, not yet attached to a Session, so that the tests" +
                    " are able to always start with a fresh UI with a pre-known state. Perhaps you're " +
                    "using Spring which reuses a scoped instance of the UI?"
        }

        // remember this tab's window.name eagerly (the faked ECD is populated lazily); MockPage
        // reads it back when faking ExtendedClientDetails, and MockBrowser uses it to identify tabs.
        ComponentUtil.setData(ui, WINDOW_NAME_KEY, windowName)

        // hook into Page.reload() and recreate the UI
        UI::class.java.getDeclaredField("page").apply {
            isAccessible = true
            set(ui, MockPage(ui, uiFactory, session))
        }
        ui.internals.session = session
        UI.setCurrent(ui)
        // see the uiId parameter kdoc: it is the key in VaadinSession.uIs and must not collide with
        // any UI already there (the transient two-live-UI window on F5, or other open browser tabs).
        ui.doInit(request, uiId, "ROOT-1")
        strongRefUI.set(ui)

        session.addUI(ui)
        session.service.fireUIInitListeners(ui)

        // Real Vaadin performs a client-side roundtrip before navigating to the main route.
        // This has the following effect: if UIInitListener fetches ExtendedClientDetails, this
        // is fetched before the main route is navigated to => ECD is available in main route's constructor.
        // Emulate this.
        clientRoundtrip()

        // navigate to the initial page
        if (lastUILocation.get() != null) {
            UI.getCurrent().internals.router.navigate(UI.getCurrent(), lastUILocation.get()!!, NavigationTrigger.PROGRAMMATIC)
            lastUILocation.remove()
        } else {
            if (KaribuConfig.initDefaultRoute && UI.getCurrent().internals.router.registry.getNavigationTarget("").isPresent) {
                UI.getCurrent().navigate("")
            }
        }

        // make sure that UI.getCurrent().push() can be called.
        // https://github.com/mvysny/karibu-testing/issues/80
        ui.pushConfiguration.pushMode = PushMode.AUTOMATIC
    }

    /**
     * Since Karibu-Testing runs in the same JVM as the server and there is no browser, the boundaries between the client and
     * the server become unclear. When looking into sources of any test method, it's really hard to tell where exactly the server request ends, and
     * where another request starts.
     *
     * You can establish an explicit client boundary in your test, by explicitly calling this method. However, since that
     * would be both laborous and error-prone, the default operation is that Karibu Testing pretends as if there was a client-server
     * roundtrip before every component lookup
     * via the [_get]/[_find]/[_expectNone]/[_expectOne] call. See [TestingLifecycleHook] for more details.
     *
     * Calls the following:
     * * [runUIQueue]
     * * [StateTree.runExecutionsBeforeClientResponse] which runs all blocks scheduled via [UI.beforeClientResponse]
     * * [cleanupDialogs]
     *
     * If you'd like to test your [ErrorHandler] then take a look at [runUIQueue] instead.
     * @param propagateExceptionToHandler defaults to false. Controls what happens to an exception
     * thrown from a [Command] scheduled via [UI.access]:
     * * **false (default):** Karibu captures any such exception and re-throws it from this method,
     * so it fails your test (fail-fast). This is the right choice for normal testing, where
     * background jobs aren't expected to throw and an accidental exception should surface as a test
     * failure.
     * * **true:** the exception is instead routed to your [VaadinSession.errorHandler] and is not
     * re-thrown - exactly as Vaadin behaves in production. Use this when you want to verify that
     * error handling works end-to-end the way it would in production: either you're testing a
     * custom [VaadinSession.errorHandler] directly (e.g. asserting it shows a notification), or
     * you're checking that your app's background jobs have their failures handled correctly. Note:
     * `true` only takes effect when a non-[DefaultErrorHandler] [VaadinSession.errorHandler] is
     * installed; otherwise Karibu still fails the test so the exception isn't silently swallowed.
     * @throws IllegalStateException if the environment is not mocked
     */
    @JvmOverloads
    @JvmStatic
    public fun clientRoundtrip(propagateExceptionToHandler: Boolean = false) {
        checkNotNull(VaadinSession.getCurrent()) { "No VaadinSession" }
        runUIQueue(propagateExceptionToHandler)
        UI.getCurrent().internals.stateTree.runExecutionsBeforeClientResponse()
        cleanupDialogs()
        KaribuConfig.testingLifecycleHook.handlePendingJavascriptInvocations(UI.getCurrent().internals.dumpPendingJavaScriptInvocations())
    }

    /**
     * Runs all tasks scheduled by [UI.access].
     *
     * If [VaadinSession.errorHandler] is not set or [propagateExceptionToHandler]
     * is false, any exceptions thrown from [Command]s scheduled via the [UI.access] will make this function fail.
     * The exceptions will be wrapped in [ExecutionException]. Generally
     * it's best to keep [propagateExceptionToHandler] set to false to
     * make any exceptions fail the test; however if you're testing
     * how your own custom [VaadinSession.errorHandler] responds to exceptions then
     * set this parameter to true.
     *
     * Called automatically by [clientRoundtrip] which is by default called automatically from [TestingLifecycleHook]. You generally
     * don't need to call this method unless you need to test your [ErrorHandler].
     *
     * @param propagateExceptionToHandler defaults to false. Controls what happens to an exception
     * thrown from a [Command] scheduled via [UI.access]:
     * * **false (default):** Karibu captures any such exception and re-throws it from this method,
     * so it fails your test (fail-fast). This is the right choice for normal testing, where
     * background jobs aren't expected to throw and an accidental exception should surface as a test
     * failure.
     * * **true:** the exception is instead routed to your [VaadinSession.errorHandler] and is not
     * re-thrown - exactly as Vaadin behaves in production. Use this when you want to verify that
     * error handling works end-to-end the way it would in production: either you're testing a
     * custom [VaadinSession.errorHandler] directly (e.g. asserting it shows a notification), or
     * you're checking that your app's background jobs have their failures handled correctly. Note:
     * `true` only takes effect when a non-[DefaultErrorHandler] [VaadinSession.errorHandler] is
     * installed; otherwise Karibu still fails the test so the exception isn't silently swallowed.
     * @throws IllegalStateException if the environment is not mocked
     */
    @JvmOverloads
    @JvmStatic
    public fun runUIQueue(propagateExceptionToHandler: Boolean = false, session: VaadinSession = VaadinSession.getCurrent()) {
        // we need to set up UI error handler which will be notified for every exception thrown out of the acccess{} block
        // otherwise the exceptions would simply be logged but unlock() wouldn't fail.
        val errors: MutableList<Throwable> = mutableListOf<Throwable>()
        val oldErrorHandler: ErrorHandler? = session.errorHandler
        if (oldErrorHandler == null || oldErrorHandler is DefaultErrorHandler || !propagateExceptionToHandler) {
            session.errorHandler = ErrorHandler {
                var t: Throwable = it.throwable
                if (t !is ExecutionException) {
                    // for some weird reason t may not be ExecutionException when it originates from a coroutine :confused:
                    // the stacktrace would point someplace random. Wrap it in ExecutionException whose stacktrace will point to the test
                    t = ExecutionException(t.message, t)
                }
                errors.add(t)
            }
        }

        try {
            // make sure the lock is held exactly once, otherwise the session.unlock() won't
            // process all Runnables registered via ui.access()
            expect(1) { (session.lockInstance as ReentrantLock).holdCount }
            session.unlock()  // this will process all Runnables registered via ui.access()
            // lock the session back, so that the test can continue running as-if in the UI thread.
            session.lock()
        } finally {
            session.errorHandler = oldErrorHandler
        }

        if (errors.isNotEmpty()) {
            errors.drop(1).forEach { errors[0].addSuppressed(it) }
            throw errors[0]
        }
    }

    /**
     * Internal function, do not call directly.
     *
     * Only usable when you are providing your own implementation of [VaadinSession].
     * See [MockVaadinSession] on how to call this properly.
     */
    @JvmStatic
    public fun afterSessionClose(session: VaadinSession, uiFactory: () -> UI) {
        // We need to simulate the actual browser + servlet container behavior here.
        // Imagine that we want a test scenario where the user logs out, and we want to check that a login prompt appears.

        // To log out the user, the code typically closes the session and tells the browser to reload
        // the page (Page.getCurrent().reload() or similar).
        // Thus the page is reloaded by the browser, and since the session is gone, the servlet container
        // will create a new, fresh session.

        // That's exactly what we need to do here. We need to close the current UI and eradicate it,
        // then we need to close the current session and eradicate it, and then we need to create a completely fresh
        // new UI and Session.

        // A problem appears when the uiFactory accidentally doesn't create a new, fresh instance of UI. Say that
        // we call Spring injector to provide us an instance of the UI, but we accidentally scoped the UI to Session.
        // Spring doesn't know that (since we haven't told Spring that the Session scope is gone) and provides
        // the previous UI instance which is still attached to the session. And it blows.

        if (!currentlyClosingSession.get()) {
            // Vaadin 20.0.5+: closing session also clears the wrapped VaadinSession.getSession().
            // Acquire the wrapped session beforehand.
            val mockSession: FakeHttpSession = session.fake
            clearVaadinInstances(true)
            mockSession.destroy()
            createSession(mockSession.servletContext, uiFactory)
        }
    }
}

private val _VaadinService_sessionInitListeners: Field by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val field: Field = VaadinService::class.java.getDeclaredField("sessionInitListeners")
    field.isAccessible = true
    field
}

private fun VaadinService.fireSessionInitListeners(event: SessionInitEvent) {
    @Suppress("UNCHECKED_CAST")
    val sessionInitListeners: Collection<SessionInitListener> =
        _VaadinService_sessionInitListeners.get(this) as Collection<SessionInitListener>
    for (sessionInitListener in sessionInitListeners) {
        sessionInitListener.sessionInit(event)
    }
}

private val _VaadinService_sessionDestroyListeners: Field by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val field: Field = VaadinService::class.java.getDeclaredField("serviceDestroyListeners")
    field.isAccessible = true
    field
}

private fun VaadinService.fireServiceDestroyListeners(event: ServiceDestroyEvent) {
    @Suppress("UNCHECKED_CAST")
    val listeners: Collection<ServiceDestroyListener> =
        _VaadinService_sessionDestroyListeners.get(this) as Collection<ServiceDestroyListener>
    for (listener in listeners) {
        listener.serviceDestroy(event)
    }
}

/**
 * Stores a UI's `window.name` (browser tab identity) as component data, populated eagerly when the
 * UI is created (unlike the *lazily* faked [ExtendedClientDetails], which only materializes when the
 * app asks for it). This is the source of truth [MockBrowser] uses to identify tabs; the faked ECD
 * honors the same value. See [KaribuConfig.windowName].
 */
internal const val WINDOW_NAME_KEY: String =
    "com.github.mvysny.kaributesting.v10.windowName"

/**
 * The `window.name` (browser tab identity) of this UI, as assigned when the UI was created by
 * [MockVaadin.createUI]. See [WINDOW_NAME_KEY]. Empty string if the UI was not created by Karibu.
 */
internal val UI.windowName: String
    get() = ComponentUtil.getData(this, WINDOW_NAME_KEY) as? String ?: ""

private class MockPage(private val ui: UI, private val uiFactory: () -> UI, private val session: VaadinSession) : Page(ui) {
    override fun reload() {
        // recreate the UI on reload(), to simulate browser's F5
        super.reload()
        MockVaadin.reloadCurrentUI(uiFactory, session)
    }

    /**
     * Karibu-Testing-faked retrieval of [ExtendedClientDetails].
     *
     * Note that the
     * retrieval is emulated in a way as if it executed "asynchronously" - the
     * [receiver] closure isn't run immediately (unless the ECD was already fetched).
     * To run the [receiver], call [MockVaadin.clientRoundtrip].
     *
     * Also see [fakeExtendedClientDetails] for more details.
     */
    override fun retrieveExtendedClientDetails(receiver: ExtendedClientDetailsReceiver) {
        if (!KaribuConfig.fakeExtendedClientDetails) {
            super.retrieveExtendedClientDetails(receiver)
            return
        }

        if (ui.internals.extendedClientDetails.initialized) {
            // already retrieved. just call super - it will call receiver right away.
            super.retrieveExtendedClientDetails(receiver)
            return
        }

        // need to call this lazily: https://github.com/mvysny/karibu-testing/issues/184#issuecomment-2639789774
        ui.access {
            // construct mock ExtendedClientDetails then set it to ui.internals. Honor this tab's
            // window.name (assigned at UI creation) so the faked ECD matches the tab identity.
            val ecd = createExtendedClientDetails(windowName = ui.windowName)
            ui.internals.extendedClientDetails = ecd
            super.retrieveExtendedClientDetails(receiver)
        }
    }
}
